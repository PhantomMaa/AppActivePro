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

import io.appactive.demo.common.entity.Order;
import io.appactive.demo.common.entity.ResultHolder;
import io.appactive.demo.common.service.dubbo.OrderService;
import io.appactive.demo.order.repository.OrderRepository;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@DubboService(version = "1.0.0", group = "appactive", parameters = {"rsActive", "unit", "routeIndex", "0"})
public class OrderServiceImpl implements OrderService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    OrderRepository orderRepository;

    @Override
    public ResultHolder<Order> order(String rId, String name, Integer number) {
        try {
            Order order = new Order();
            order.setUserId(Long.valueOf(rId));
            order.setOrderDate(new Date());
            order.setName(name);
            order.setStatus("payed");
            order = orderRepository.save(order);
            if (order.getOrderId() == null) {
                logger.warn("save into db fail");
                return ResultHolder.fail("save into db fail");
            }

            return ResultHolder.succeed(order);
        } catch (Throwable e) {
            logger.warn("order fail", e);
            return ResultHolder.fail("order fail");
        }
    }
}
