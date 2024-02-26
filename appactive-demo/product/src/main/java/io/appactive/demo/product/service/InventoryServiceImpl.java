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

package io.appactive.demo.product.service;

import io.appactive.demo.common.entity.Product;
import io.appactive.demo.common.entity.ResultHolder;
import io.appactive.demo.common.service.dubbo.InventoryService;
import io.appactive.demo.product.repository.ProductRepository;
import io.appactive.support.log.LogUtil;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
@DubboService(version = "1.0.0", group = "appactive", parameters = {"rsActive", "center", "routeIndex", "0"})
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LogUtil.getLogger();

    @Resource
    ProductRepository productRepository;

    @Override
    public ResultHolder<Product> decrease(String rId, String pId, Integer number) {
        logger.info("decrease rId : {}, pId : {}, number : {}", rId, pId, number);
        try {
            Optional<Product> op = productRepository.findById(pId);
            if (!op.isPresent()) {
                logger.warn("no such product");
                return ResultHolder.fail("no such product");
            }

            // 扣库存
            Product p = op.get();
            int oldNum = p.getNumber();
            int left = oldNum - number;
            if (left < 0) {
                logger.warn("sold out");
                return new ResultHolder<>(p, false, "sold out");
            }

            p.setNumber(left);
            p = productRepository.save(p);
            if (p.getNumber() + number != oldNum) {
                logger.warn("storage not consist");
                return new ResultHolder<>(p, false, "storage not consist");
            } else {
                return ResultHolder.succeed(p);
            }
        } catch (Throwable e) {
            String errMessage = e.getCause().getCause().getMessage();
            logger.warn("exception: {}", errMessage);
            return ResultHolder.fail(errMessage);
        }
    }
}
