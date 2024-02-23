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
import io.appactive.demo.frontend.service.FrontEndService;
import io.appactive.java.api.base.AppContextClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("/")
public class FrontController {

    @Autowired
    private FrontEndService frontEndService;

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private Environment env;

    private Map<String, String[]> metaData;

    @RequestMapping("/")
    public String index() {
        return "redirect:/listProduct";
    }

    @GetMapping("/list")
    @ResponseBody
    public ResultHolder<List<Product>> list() {
        // normal
        return frontEndService.list();
    }

    @GetMapping(value = "/detail")
    @ResponseBody
    public ResultHolder<Product> detail(@RequestParam(required = false, defaultValue = "12") String id) {
        // unit
        return frontEndService.detail(AppContextClient.getRouteId(), id);
    }

    @RequestMapping("/buy")
    @ResponseBody
    public ResultHolder<String> buy(@RequestParam(required = false, defaultValue = "12") String id,
                                    @RequestParam(required = false, defaultValue = "5") Integer number) {
        // unit
        return frontEndService.buy(id, number);
    }

    @RequestMapping("/echo")
    @ResponseBody
    public ResultHolder<String> echo(
            @RequestParam(required = false, defaultValue = "echo content") String content
    ) {
        return new ResultHolder<>(appName + " : " + content);
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
        return new ResultHolder<>(metaData);
    }

    @GetMapping("/listProduct")
    public String listProduct(Model model) {
        // normal
        ResultHolder<List<Product>> resultHolder = frontEndService.list();

        model.addAttribute("result", JSON.toJSONString(resultHolder.getResult()));
        model.addAttribute("products", resultHolder.getResult());
        model.addAttribute("chain", JSON.toJSONString(resultHolder.getChain()));
        model.addAttribute("current", "listProduct");
        return "index.html";
    }

    @GetMapping(value = "/detailProduct")
    public String detailProduct(@RequestParam(required = false, defaultValue = "12") String id,
                                @RequestParam(required = false, defaultValue = "false") Boolean hidden,
                                Model model) {
        // unit
        ResultHolder<Product> resultHolder = getProductResultHolder(id, hidden);

        model.addAttribute("result", JSON.toJSONString(resultHolder.getResult()));
        model.addAttribute("product", resultHolder.getResult());
        model.addAttribute("chain", JSON.toJSONString(resultHolder.getChain()));
        model.addAttribute("current", "detailProduct");
        return "detail.html";
    }

    private ResultHolder<Product> getProductResultHolder(String id, Boolean hidden) {
        return hidden ? frontEndService.detailHidden(id) : frontEndService.detail(AppContextClient.getRouteId(), id);
    }

    @RequestMapping("/buyProduct")
    public String buyProduct(@RequestParam(required = false, defaultValue = "12") String pId,
                             @RequestParam(required = false, defaultValue = "1") Integer number, Model model) {
        ResultHolder<String> resultHolder = frontEndService.buy(pId, number);
        ResultHolder<Product> productHolder = getProductResultHolder(pId, false);

        model.addAttribute("result", JSON.toJSONString(resultHolder.getResult()));
        model.addAttribute("msg", resultHolder.getResult());
        model.addAttribute("product", productHolder.getResult());
        model.addAttribute("chain", JSON.toJSONString(resultHolder.getChain()));
        model.addAttribute("current", "buyProduct");
        return "buy.html";
    }


}
