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

package io.appactive.demo.order;

import io.appactive.demo.common.entity.Order;
import io.appactive.demo.common.entity.ResultHolder;
import io.appactive.demo.common.service.dubbo.OrderService;
import io.appactive.java.api.base.AppContextClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@SpringBootApplication
@EntityScan("io.appactive.demo.*")
@ComponentScan(basePackages = {
        "io.appactive.demo",
})
@Controller("/")
public class OrderApplication {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Resource
    OrderService orderService;

    @Value("${spring.application.name}")
    private String appName;

    @RequestMapping("/buy")
    @ResponseBody
    public ResultHolder<String> buy(@RequestParam(required = false, defaultValue = "1000") String rId,
                                    @RequestParam(required = false, defaultValue = "14") String id,
                                    @RequestParam(required = false, defaultValue = "1") Integer number) {
        String routerId = AppContextClient.getRouteId();
        logger.info("buy, routerId: {}, pid: {}, number: {}", routerId, id, number);
        ResultHolder<Order> resultHolder = orderService.order(rId, id, number);

        ResultHolder<String> result = new ResultHolder<>();
        result.setResult(String.format("routerId %s bought %d of item %s, result: %s", routerId, number, id, resultHolder.getResult()));
        return result;
    }

    @RequestMapping("/check")
    @ResponseBody
    public String check() {
        return "OK From " + appName;
    }
}
