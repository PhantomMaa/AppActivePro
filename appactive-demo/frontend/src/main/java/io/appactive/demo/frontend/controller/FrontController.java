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

package io.appactive.demo.frontend.controller;

import com.alibaba.fastjson.JSON;
import io.appactive.demo.common.entity.Product;
import io.appactive.demo.common.entity.ResultHolder;
import io.appactive.demo.frontend.service.FrontendManager;
import io.appactive.java.api.base.AppContextClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("/")
public class FrontController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private FrontendManager frontendManager;

    @Value("${spring.application.name}")
    private String appName;

    @Resource
    private Environment env;

    private Map<String, String[]> metaData;

    @RequestMapping("/")
    public String index() {
        return "redirect:/listProduct";
    }

    @RequestMapping("/echo")
    @ResponseBody
    public ResultHolder<String> echo(@RequestParam(required = false, defaultValue = "echo content") String content) {
        return ResultHolder.succeed(appName + " : " + content);
    }

    @RequestMapping("/check")
    @ResponseBody
    public String check() {
        return "OK From " + appName;
    }

    @RequestMapping("/show")
    @ResponseBody
    public String show() {
        return "routerId: " + AppContextClient.getRouteId();
    }


    @ModelAttribute("metaData")
    public Map<String, String[]> getMetaData() {
        return metaData;
    }

    @PostConstruct
    public void parseMetaData() {
        String unitList = env.getProperty("io.appactive.demo.unitlist");
        String appList = env.getProperty("io.appactive.demo.applist");
        metaData = new HashMap<>(2);
        assert unitList != null;
        assert appList != null;

        metaData.put("unitList", unitList.split(","));
        metaData.put("appList", appList.split(","));
    }

    @RequestMapping("/meta")
    @ResponseBody
    public ResultHolder<Object> meta() {
        return ResultHolder.succeed(metaData);
    }

    @GetMapping("/listProduct")
    public String listProduct(Model model) {
        // normal
        ResultHolder<List<Product>> resultHolder = frontendManager.list();

        model.addAttribute("result", JSON.toJSONString(resultHolder.getResult()));
        model.addAttribute("products", resultHolder.getResult());
        model.addAttribute("chain", JSON.toJSONString(resultHolder.getChain()));
        model.addAttribute("current", "listProduct");
        return "index.html";
    }

    @GetMapping(value = "/detailProduct")
    public String detailProduct(@RequestParam(required = false, defaultValue = "12") String id, Model model) {
        ResultHolder<Product> resultHolder = frontendManager.detail(AppContextClient.getRouteId(), id);
        String chain = JSON.toJSONString(resultHolder.getChain());
        model.addAttribute("result", JSON.toJSONString(resultHolder.getResult()));
        model.addAttribute("product", resultHolder.getResult());
        model.addAttribute("chain", JSON.toJSONString(resultHolder.getChain()));
        model.addAttribute("current", "detailProduct");
        logger.info("chain : {}", chain);
        return "detail.html";
    }


    @GetMapping(value = "/manageProduct")
    public String manageProduct(@RequestParam(required = false, defaultValue = "12") String id, Model model) {
        ResultHolder<Product> resultHolder = frontendManager.detail(AppContextClient.getRouteId(), id);
        String chain = JSON.toJSONString(resultHolder.getChain());
        model.addAttribute("result", JSON.toJSONString(resultHolder.getResult()));
        model.addAttribute("product", resultHolder.getResult());
        model.addAttribute("chain", JSON.toJSONString(resultHolder.getChain()));
        model.addAttribute("current", "manageProduct");
        logger.info("chain : {}", chain);
        return "manage.html";
    }

    @RequestMapping("/decrease")
    public String decrease(@RequestParam(required = false, defaultValue = "12") String pId, @RequestParam(required = false, defaultValue = "1") Integer number, Model model) {
        ResultHolder<Product> resultHolder = frontendManager.decrease(pId, number);
        String chain = JSON.toJSONString(resultHolder.getChain());
        model.addAttribute("message", resultHolder.getMessage());
        model.addAttribute("product", resultHolder.getResult());
        model.addAttribute("chain", chain);
        model.addAttribute("current", "decrease");
        logger.info("chain : {}", chain);
        return "decrease.html";
    }


    @RequestMapping("/order")
    public String order(@RequestParam(required = false, defaultValue = "12") String pId, @RequestParam(required = false, defaultValue = "1") Integer number, Model model) {
        ResultHolder<Product> resultHolder = frontendManager.order(pId, number);
        String chain = JSON.toJSONString(resultHolder.getChain());
        model.addAttribute("message", resultHolder.getMessage());
        model.addAttribute("product", resultHolder.getResult());
        model.addAttribute("chain", chain);
        model.addAttribute("current", "decrease");
        logger.info("chain : {}", chain);
        return "order.html";
    }


}
