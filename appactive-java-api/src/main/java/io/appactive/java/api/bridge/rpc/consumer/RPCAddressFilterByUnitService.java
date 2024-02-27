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

package io.appactive.java.api.bridge.rpc.consumer;

import java.util.List;
import java.util.Set;


public interface RPCAddressFilterByUnitService<T> {

    void initAddressCallBack(RPCAddressCallBack<T> callBack);

    /**
     * init service list
     *
     * @param servicePrimaryName primaryKey, dubbo: service+group+verison
     * @param list               service remote ip list
     * @param version            version of config itself, which can be used to reduce calculation.
     *                           null means we need to calculate list every time
     * @return did this method actually do the calculating
     */
    Boolean refreshAddressList(String servicePrimaryName, List<T> list, String version);

    /**
     * used with refreshAddressList， filter address
     *
     * @param servicePrimaryName primaryKey, dubbo: service+group+verison
     * @param routeId            as it is
     * @return address list
     */
    List<T> addressFilter(String servicePrimaryName, String routeId);

    Boolean emptyCache(String servicePrimaryName);

    String getMetaMapFromServer(T server, String key);

    Set<String> getCachedServicePrimaryNames();

}
