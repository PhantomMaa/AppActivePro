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


public interface RulePropertyConstant {

    String PROPERTY_HEADER = "appactive";

    /**
     * unit-flag from jvm/env key
     */
    String UNIT_LAG_PROPERTY_KEY = PROPERTY_HEADER + ".unit";

    /**
     * nacos
     */
    String DATA_ID_HEADER = PROPERTY_HEADER + ".dataId";
    String GROUP_ID = PROPERTY_HEADER + ".groupId";
    String NAMESPACE_ID = PROPERTY_HEADER + ".namespaceId";
    String LOCAL_NACOS = "127.0.0.1:8848";

}
