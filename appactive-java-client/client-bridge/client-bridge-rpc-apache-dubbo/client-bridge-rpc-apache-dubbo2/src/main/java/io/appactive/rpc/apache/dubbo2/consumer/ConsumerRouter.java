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

package io.appactive.rpc.apache.dubbo2.consumer;

import io.appactive.java.api.base.AppContextClient;
import io.appactive.java.api.base.exception.ExceptionFactory;
import io.appactive.java.api.bridge.rpc.constants.constant.RPCConstant;
import io.appactive.java.api.bridge.rpc.consumer.RPCAddressFilterByUnitService;
import io.appactive.java.api.rule.traffic.TrafficRouteRuleService;
import io.appactive.rpc.apache.dubbo2.consumer.callback.Dubbo2AddressCallBack;
import io.appactive.rpc.base.consumer.RPCAddressFilterByUnitServiceImpl;
import io.appactive.rule.ClientRuleService;
import io.appactive.support.lang.CollectionUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;

public class ConsumerRouter implements Router {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int CONSUMER_ROUTER_DEFAULT_PRIORITY = -10;

    private final URL url;

    private final TrafficRouteRuleService trafficRouteRuleService = ClientRuleService.getTrafficRouteRuleService();

    private RPCAddressFilterByUnitService addressFilterByUnitService = new RPCAddressFilterByUnitServiceImpl();

    private final Dubbo2AddressCallBack callBack = new Dubbo2AddressCallBack();

    private final String servicePrimaryKey;

    private Integer routeIdIndex = null;

    public ConsumerRouter(URL referenceUrl) {
        this.url = referenceUrl;
        this.servicePrimaryKey = referenceUrl.getServiceKey();

        logger.info("init-ConsumerUnitRouter, url : {}", url);
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        String indexValue = null;
        if (this.routeIdIndex != null && this.routeIdIndex >= 0) {
            // 获得显示透传的route
            indexValue = getRouteIndexValue(invocation);
        } else {
            // 隐式透传
            RpcContext.getContext().setAttachment(RPCConstant.CONSUMER_REMOTE_ROUTE_ID_KEY, AppContextClient.getRouteId());
        }

        return (List<Invoker<T>>) addressFilterByUnitService.addressFilter(servicePrimaryKey, indexValue);
    }

    @Override
    public boolean isRuntime() {
        return true;
    }

    @Override
    public boolean isForce() {
        return false;
    }

    @Override
    public int getPriority() {
        return CONSUMER_ROUTER_DEFAULT_PRIORITY;
    }

    @Override
    public <T> void notify(List<Invoker<T>> invokers) {
        initRouteIdIndex(invokers);

        if (!trafficRouteRuleService.haveTrafficRule()) {
            // 非单元化，不处理
            return;
        }

        addressFilterByUnitService.initAddressCallBack(callBack);
        addressFilterByUnitService.refreshAddressList(servicePrimaryKey, invokers, null, null);
    }

    private <T> void initRouteIdIndex(List<Invoker<T>> invokers) {
        if (this.routeIdIndex != null) {
            return;
        }

        if (CollectionUtils.isEmpty(invokers)) {
            return;
        }

        for (Invoker<T> invoker : invokers) {
            String metaMapValue = callBack.getMetaMapValue(invoker, RPCConstant.URL_ROUTE_INDEX_KEY);
            if (metaMapValue != null) {
                this.routeIdIndex = Integer.parseInt(metaMapValue);
                return;
            }
        }
        this.routeIdIndex = -1;
    }

    private String getRouteIndexValue(Invocation invocation) {
        try {
            return String.valueOf(invocation.getArguments()[routeIdIndex]);
        } catch (Throwable throwable) {
            String msg = MessageFormat.format("service:{0}, error when get routeId in params, routeIdIndex:{1}",
                    servicePrimaryKey, routeIdIndex);
            throw ExceptionFactory.makeFault(msg, throwable);
        }
    }

}
