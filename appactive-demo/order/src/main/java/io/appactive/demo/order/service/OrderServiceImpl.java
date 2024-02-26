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

package io.appactive.demo.order.service;

import io.appactive.demo.common.entity.Product;
import io.appactive.demo.common.entity.ResultHolder;
import io.appactive.demo.common.service.dubbo.OrderService;
import io.appactive.demo.order.repository.ProductRepository;
import io.appactive.support.log.LogUtil;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
@DubboService(version = "1.0.0", group = "appactive", parameters = {"rsActive", "center", "routeIndex", "0"})
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LogUtil.getLogger();

    @Resource
    ProductRepository productRepository;

    @Override
    public ResultHolder<Boolean> buy(String rId, String pId, Integer number) {
        try {
            Optional<Product> op = productRepository.findById(pId);
            if (!op.isPresent()) {
                logger.warn("no such product");
                return new ResultHolder<>(false);
            }

            // 扣库存
            Product p = op.get();
            int oldNum = p.getNumber();
            int left = oldNum - number;
            if (left < 0) {
                logger.warn("sold out");
                return new ResultHolder<>(false);
            }

            p.setNumber(left);
            p = productRepository.save(p);
            if (p.getNumber() + number != oldNum) {
                logger.warn("storage not consist");
                return new ResultHolder<>(false);
            } else {
                return new ResultHolder<>(true);
            }
        } catch (Throwable e) {
            logger.warn("exception: {}", e.getCause().getCause().getMessage());
            return new ResultHolder<>(false);
        }
    }
}
