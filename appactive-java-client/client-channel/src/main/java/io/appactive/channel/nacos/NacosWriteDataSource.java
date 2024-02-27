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

package io.appactive.channel.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import io.appactive.java.api.channel.ConfigWriteDataSource;
import io.appactive.java.api.channel.ConverterInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class NacosWriteDataSource<T> implements ConfigWriteDataSource<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConverterInterface<T, String> converterInterface;

    private final String serverAddr;
    private final String dataId;
    private final String groupId;
    private final String namespaceId;

    private ConfigService configService;

    public NacosWriteDataSource(String serverAddr, String dataId, String groupId, ConverterInterface<T, String> converterInterface) {
        this(serverAddr,dataId,groupId,"", converterInterface);
    }

    public NacosWriteDataSource(String serverAddr, String dataId, String groupId, String namespaceId, ConverterInterface<T, String> converterInterface) {
        this.serverAddr = serverAddr;
        this.dataId = dataId;
        this.groupId = groupId;
        this.namespaceId = namespaceId;
        this.converterInterface = converterInterface;

        startTimerService();
    }

    private void startTimerService() {
        try {
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
            properties.put(PropertyKeyConst.NAMESPACE, namespaceId);
            configService = NacosFactory.createConfigService(properties);
        } catch (NacosException e) {
            logger.error("get Nacos configService Exception ", e);
        }
    }

    @Override
    public boolean write(T value) throws Exception {
        return write(value, ConfigType.JSON.getType());
    }

    @Override
    public boolean write(T value, String type) throws Exception {
        return syncWriteNacos(value,type);
    }

    private boolean syncWriteNacos(T value, String type) {
        try {
            String convertResult = converterInterface.convert(value);
            return configService.publishConfig(dataId, groupId, convertResult, type);
        }catch (Exception e){
            logger.error("write Nacos config Exception ", e);
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        if (configService!=null){
            configService.shutDown();
        }
    }
}
