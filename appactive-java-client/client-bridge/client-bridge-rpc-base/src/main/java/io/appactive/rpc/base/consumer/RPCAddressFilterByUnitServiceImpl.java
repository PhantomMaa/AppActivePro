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

package io.appactive.rpc.base.consumer;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import io.appactive.java.api.base.exception.AppactiveException;
import io.appactive.java.api.bridge.rpc.constants.constant.RPCConstant;
import io.appactive.java.api.bridge.rpc.consumer.RPCAddressCallBack;
import io.appactive.java.api.bridge.rpc.consumer.RPCAddressFilterByUnitService;
import io.appactive.java.api.base.constants.AppactiveConstant;
import io.appactive.java.api.base.AppContextClient;
import io.appactive.java.api.base.constants.ResourceActiveType;
import io.appactive.java.api.rule.machine.AbstractMachineUnitRuleService;
import io.appactive.java.api.rule.traffic.TrafficRouteRuleService;
import io.appactive.java.api.utils.lang.StringUtils;
import io.appactive.rpc.base.consumer.bo.AddressActive;
import io.appactive.rule.ClientRuleService;
import io.appactive.support.lang.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RPCAddressFilterByUnitServiceImpl<T> implements RPCAddressFilterByUnitService<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private RPCAddressCallBack<T> rpcUnitCellCallBack;

    private static final String NO_UNIT_LABEL_PROVIDER_TAG_NAME = "NO_UNIT_FLAG_LABEL";

    private final Map<String, AddressActive<T>> SERVICE_REMOTE_ADDRESS_MAP = new ConcurrentHashMap<>();

    private final TrafficRouteRuleService trafficRouteRuleService = ClientRuleService.getTrafficRouteRuleService();

    private final AbstractMachineUnitRuleService machineUnitRuleService = ClientRuleService.getMachineUnitRuleService();

    @Override
    public void initAddressCallBack(RPCAddressCallBack<T> callBack) {
        this.rpcUnitCellCallBack = callBack;
    }

    @Override
    public Boolean refreshAddressList(String servicePrimaryName, List<T> list, String version) {
        if (CollectionUtils.isEmpty(list)) {
            emptyCache(servicePrimaryName);
        }
        if (version != null && SERVICE_REMOTE_ADDRESS_MAP.containsKey(servicePrimaryName)) {
            AddressActive<T> addressActive = SERVICE_REMOTE_ADDRESS_MAP.get(servicePrimaryName);
            if (list.equals(addressActive.getOriginalList())
                    && version.equalsIgnoreCase(getMetaMapFromServer(addressActive.getOriginalList().get(0), RPCConstant.SPRING_CLOUD_SERVICE_META_VERSION))) {
                // both servers and uris equals with current ones, no need to refresh
                return false;
            }
        }

        String resourceActiveType = getResourceActiveType(list);
        Map<String, List<T>> unitServersMap = transToUnitFlagServerListMap(list);

        SERVICE_REMOTE_ADDRESS_MAP.put(servicePrimaryName, new AddressActive<>(resourceActiveType, unitServersMap, list));
        logger.info("servicePrimaryName : {}, cache got refreshed, new version : {}", servicePrimaryName, version);
        logger.info("SERVICE_REMOTE_ADDRESS_MAP : {}", JSON.toJSONString(SERVICE_REMOTE_ADDRESS_MAP));
        return true;
    }

    @Override
    public List<T> addressFilter(String servicePrimaryName, String routeId) {
        AddressActive<T> addressActive = SERVICE_REMOTE_ADDRESS_MAP.get(servicePrimaryName);
        if (addressActive == null) {
            return null;
        }

        String resourceType = addressActive.getResourceActiveType();
        Map<String, List<T>> unitServersMap = addressActive.getUnitServersMap();

        List<T> originalList = addressActive.getOriginalList();
        List<T> result = getFilterResult(servicePrimaryName, resourceType, unitServersMap, originalList, routeId);

        logServer(result);
        return result;
    }

    @Override
    public Boolean emptyCache(String servicePrimaryName) {
        SERVICE_REMOTE_ADDRESS_MAP.remove(servicePrimaryName);
        return true;
    }


    private List<T> getFilterResult(String servicePrimaryName, String resourceType, Map<String, List<T>> unitServersMap, List<T> originalServers, String routeId) {
        if (ResourceActiveType.UNIT_RESOURCE_TYPE.equalsIgnoreCase(resourceType)) {
            return unitServers(unitServersMap, servicePrimaryName, routeId);
        } else {
//            return centerServers(unitServersMap, servicePrimaryName);
            return commonServers(unitServersMap, originalServers);
        }
    }

    private List<T> commonServers(Map<String, List<T>> unitServersMap, List<T> originalServers) {
        // consumer 本机所在单元
        String currentUnit = machineUnitRuleService.getCurrentUnit();
        if (currentUnit == null) {
            // 不存在单元标，切0后，随机调用下游
            logger.info("no unitFlag for current consumer, executing random calling");
            return originalServers;
        }

        // 存在单元标
        currentUnit = currentUnit.toUpperCase();

        List<T> currentUnitServers = unitServersMap.get(currentUnit);
        if (CollectionUtils.isEmpty(currentUnitServers)) {
            // 本单元没机器，则随机全局调用
            logger.info("no provider for current unit[{}] of consumer, executing random calling", currentUnit);
            return originalServers;
        }

        logger.info("executing current-unit-preferable calling");
        // 单元内优先调用
        return currentUnitServers;
    }


    private List<T> unitServers(Map<String, List<T>> unitServersMap, String servicePrimaryName, String routeId) {
        if (routeId == null) {
            // 显示透传没有，则取隐 线程上下文
            routeId = AppContextClient.getRouteId();
        }
        if (routeId == null) {
            // 无routeId 在多活里面 直接报错，无单元化路由目标地址
            String msg = MessageFormat.format("service : {0}, not have routeId", servicePrimaryName);
            logger.error(msg);
            throw new AppactiveException(msg);
        }

        String targetUnit = trafficRouteRuleService.getUnitByRouteId(routeId);
        if (StringUtils.isBlank(targetUnit)) {
            String msg = MessageFormat.format("service : {0}, routeId : {1}, targetUnit is null", servicePrimaryName, routeId);
            logger.error(msg);
            throw new AppactiveException(msg);
        }

        List<T> unitServers = unitServersMap.get(targetUnit.toUpperCase());
        if (CollectionUtils.isEmpty(unitServers)) {
            // 单元地址池没有目标单元的地址，不进行兜底
            String msg = MessageFormat.format("service : {0}, routeId : {1}, targetUnit : {2}, list is null", servicePrimaryName, routeId, targetUnit);
            logger.error(msg);
            throw new AppactiveException(msg);
        }

        return unitServers;
    }

    private List<T> centerServers(Map<String, List<T>> unitServersMap, String servicePrimaryName) {
        List<T> invokers = unitServersMap.get(AppactiveConstant.CENTER_FLAG);
        if (CollectionUtils.isEmpty(invokers)) {
            // 无中心服务
            String msg = MessageFormat.format("service : {0}, not have center list", servicePrimaryName);
            logger.error(msg);

            // 说明代码哪里有问题, 或者用户配置错误了writeMode
            throw new AppactiveException(msg);
        }

        return invokers;
    }

    private Map<String, List<T>> transToUnitFlagServerListMap(List<T> servers) {
        Map<String, List<T>> unitServersMap = Maps.newHashMap();
        for (T server : servers) {
            // get unitName
            String unitName = NO_UNIT_LABEL_PROVIDER_TAG_NAME;
            String metaUnitValue = getMetaMapFromServer(server, RPCConstant.URL_UNIT_LABEL_KEY);
            if (StringUtils.isNotBlank(metaUnitValue)) {
                // meta have value
                unitName = metaUnitValue;
            }
            unitName = unitName.toUpperCase();

            // add server
            List<T> currentUnitServers = unitServersMap.computeIfAbsent(unitName, k -> new ArrayList<>());
            currentUnitServers.add(server);
        }

        logger.info("unitServersMap : {}", unitServersMap);
        return unitServersMap;
    }


    @Override
    public String getMetaMapFromServer(T server, String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }

        return rpcUnitCellCallBack.getMetaMapValue(server, key);
    }

    @Override
    public Set<String> getCachedServicePrimaryNames() {
        return SERVICE_REMOTE_ADDRESS_MAP.keySet();
    }

    protected String getResourceActiveType(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        for (T invoker : list) {
            String metaMapValue = rpcUnitCellCallBack.getMetaMapValue(invoker, RPCConstant.URL_RESOURCE_ACTIVE_LABEL_KEY);
            if (StringUtils.isNotBlank(metaMapValue)) {
                return metaMapValue;
            }
        }
        return null;
    }


    private void logServer(List<T> servers) {
        if (CollectionUtils.isEmpty(servers)) {
            return;
        }
        List<String> toLog = new ArrayList<>();
        for (T server : servers) {
            String serverStr = rpcUnitCellCallBack.getServerToString(server);
            if (serverStr == null) {
                continue;
            }
            toLog.add(serverStr);
        }
        logger.info("server list after filtering : " + "{}", toLog);
    }
}
