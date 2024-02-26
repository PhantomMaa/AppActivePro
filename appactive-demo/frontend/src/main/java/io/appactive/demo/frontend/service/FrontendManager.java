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
import io.appactive.demo.common.service.dubbo.OrderService;
import io.appactive.demo.common.service.dubbo.ProductService;
import io.appactive.java.api.base.AppContextClient;
import io.appactive.support.log.LogUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FrontendManager {

    private static final Logger logger = LogUtil.getLogger();

    @DubboReference(version = "1.0.0", group = "appactive", check = false)
    private ProductService productService;

    @DubboReference(version = "1.0.0", group = "appactive", check = false)
    private OrderService orderService;

    public ResultHolder<List<Product>> list() {
        return productService.list();
    }

    public ResultHolder<Product> detail(String rId, String pId) {
        return productService.detail(rId, pId);
    }

    public ResultHolder<Product> buy(String pId, int number) {
        ResultHolder<Product> resultProduct = productService.detail(AppContextClient.getRouteId(), pId);
        if (resultProduct.getResult() == null) {
            logger.warn("cann't find product, pId : {}", pId);
            return new ResultHolder<>(null);
        }

        ResultHolder<Boolean> resultOrder = orderService.buy(AppContextClient.getRouteId(), pId, number);
        if (!resultOrder.getResult()) {
            logger.warn("buy failure, pId : {}", pId);
            return new ResultHolder<>(null);
        }

        ResultHolder<Product> resultHolder = new ResultHolder<>(resultProduct.getResult());
        resultHolder.getChain().addAll(resultOrder.getChain());
        resultHolder.getChain().addAll(resultProduct.getChain());
        return resultHolder;
    }
}
