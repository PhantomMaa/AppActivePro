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

package io.appactive.channel;

import io.appactive.channel.nacos.NacosPathUtil;
import io.appactive.channel.nacos.NacosReadDataSource;
import io.appactive.java.api.base.exception.ExceptionFactory;
import io.appactive.java.api.channel.ChannelTypeEnum;
import io.appactive.java.api.channel.ConfigReadDataSource;
import io.appactive.java.api.channel.ConverterInterface;
import io.appactive.java.api.utils.lang.StringUtils;

import java.util.Properties;

/**
 * 选择合适的channel
 */
public class ClientChannelService {

    private static final ChannelTypeEnum CHANNEL_TYPE_ENUM = ChannelTypeEnum.NACOS;

    public static <T> ConfigReadDataSource<T> getConfigReadDataSource(String uri, ConverterInterface<String, T> ruleConverterInterface) {
        if (StringUtils.isBlank(uri)) {
            throw ExceptionFactory.makeFault("uri is empty");
        }

        ConfigReadDataSource<T> configReadDataSource;
        if (CHANNEL_TYPE_ENUM == ChannelTypeEnum.NACOS) {
            PathUtil pathUtil = getPathUtil();
            Properties extra = pathUtil.getExtras();
            configReadDataSource = new NacosReadDataSource<>(pathUtil.getConfigServerAddress(), uri,
                    extra.getProperty(RulePropertyConstant.GROUP_ID), extra.getProperty(RulePropertyConstant.NAMESPACE_ID),
                    ruleConverterInterface);
        } else {
            throw ExceptionFactory.makeFault("unsupported channel:{}", CHANNEL_TYPE_ENUM.name());
        }
        return configReadDataSource;
    }

    public static PathUtil getPathUtil() {
        if (CHANNEL_TYPE_ENUM == ChannelTypeEnum.NACOS) {
            return NacosPathUtil.getInstance();
        }

        throw ExceptionFactory.makeFault("unsupported channel:{}", CHANNEL_TYPE_ENUM.name());
    }

    public static String getSubKeySplit() {
        if (CHANNEL_TYPE_ENUM == ChannelTypeEnum.NACOS) {
            return "_";
        }
        throw ExceptionFactory.makeFault("unsupported channel:{}", CHANNEL_TYPE_ENUM.name());
    }

}
