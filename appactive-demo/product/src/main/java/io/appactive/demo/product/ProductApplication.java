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

package io.appactive.demo.product;

import io.appactive.demo.common.entity.Product;
import io.appactive.demo.common.entity.ResultHolder;
import io.appactive.demo.common.service.dubbo.ProductUnitService;
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
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackages = {
        "io.appactive.demo",
})
@EntityScan("io.appactive.demo.*")
@Controller("/")
public class ProductApplication {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

    @Resource
    private ProductUnitService productUnitService;

    @Value("${spring.application.name}")
    private String appName;

    @RequestMapping("/list")
    @ResponseBody
    public ResultHolder<List<Product>> list() {
        return productUnitService.list();
    }


    @RequestMapping(value = "/detail")
    @ResponseBody
    public ResultHolder<Product> detail(@RequestParam(required = false, defaultValue = "12") String rId,
                                        @RequestParam(required = false, defaultValue = "12") String pId) {
        logger.info("detail, routerId: {}, pId: {}", AppContextClient.getRouteId(), pId);
        return productUnitService.detail(rId, pId);
    }

    @RequestMapping("/check")
    @ResponseBody
    public String check() {
        return "OK From " + appName;
    }
}
