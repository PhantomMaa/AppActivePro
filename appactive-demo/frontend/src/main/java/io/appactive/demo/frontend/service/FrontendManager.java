/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appactive.demo.frontend.service;

import io.appactive.demo.common.entity.Product;
import io.appactive.demo.common.entity.ResultHolder;
import io.appactive.demo.common.service.dubbo.InventoryService;
import io.appactive.demo.common.service.dubbo.OrderService;
import io.appactive.demo.common.service.dubbo.ProductCenterService;
import io.appactive.demo.common.service.dubbo.ProductUnitService;
import io.appactive.java.api.base.AppContextClient;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FrontendManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @DubboReference(version = "1.0.0", group = "appactive", check = false)
    private ProductCenterService productCenterService;

    @DubboReference(version = "1.0.0", group = "appactive", check = false)
    private ProductUnitService productUnitService;

    @DubboReference(version = "1.0.0", group = "appactive", check = false)
    private OrderService orderService;

    @DubboReference(version = "1.0.0", group = "appactive", check = false)
    private InventoryService inventoryService;

    public ResultHolder<List<Product>> list() {
        return productUnitService.list();
    }

    public ResultHolder<Product> detail(String rId, String pId) {
        return productUnitService.detail(rId, pId);
    }

    public ResultHolder<Product> decrease(String pId, int number) {
        ResultHolder<Product> resultInventory = inventoryService.decrease(AppContextClient.getRouteId(), pId, number);
        if (!resultInventory.getSuccess()) {
            logger.warn("buy failure, pId : {}", pId);
            return resultInventory;
        }

        resultInventory.setMessage("success");
        return resultInventory;
    }


    public ResultHolder<Product> order(String pId, int number) {
        // 先减库存，再创建订单
        ResultHolder<Product> resultInventory = inventoryService.decrease(AppContextClient.getRouteId(), pId, number);
        if (!resultInventory.getSuccess()) {
            logger.warn("buy failure, pId : {}", pId);
            return resultInventory;
        }

        ResultHolder<Product> resultHolder = new ResultHolder<>();
        resultHolder.setResult(resultInventory.getResult());

        ResultHolder<Void> resultOrder = orderService.order(AppContextClient.getRouteId(), pId, number);
        resultHolder.getChain().addAll(resultOrder.getChain());
        resultHolder.getChain().addAll(resultInventory.getChain());
        if (!resultOrder.getSuccess()) {
            logger.warn("buy failure, pId : {}", pId);
            resultHolder.setSuccess(false);
            resultHolder.setMessage(resultOrder.getMessage());
            return resultHolder;
        }

        resultHolder.setSuccess(true);
        resultHolder.setMessage("success");
        return resultHolder;
    }
}
