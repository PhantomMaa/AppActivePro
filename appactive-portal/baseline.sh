#
# Copyright 1999-2022 Alibaba Group Holding Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# sh baseline.sh 2 // 通过nacos通道，推送应用规则
# sh baseline.sh 3  // 推送网关规则

type=$1

if [ `expr $type % 2` == 0 ]
then
  dataIdPrefix="appactive.dataId."
  groupId="appactive.groupId"

  idSourceRule=$(cat ./rule/idSource.json)
  echo "$(date "+%Y-%m-%d %H:%M:%S") idSourceRule 推送结果: " \
    && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
    -d "dataId=${dataIdPrefix}idSourceRulePath&group=${groupId}&content=${idSourceRule}" \
    && echo ""

  idTransformerRule=$(cat ./rule/transformerBetween.json)
  echo "$(date "+%Y-%m-%d %H:%M:%S") idTransformerRule 推送结果: " \
    && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
    -d "dataId=${dataIdPrefix}transformerRulePath&group=${groupId}&content=${idTransformerRule}" \
    && echo ""

  idUnitMappingRule=$(cat ./rule/idUnitMapping.json)
  echo "$(date "+%Y-%m-%d %H:%M:%S") idUnitMappingRule 推送结果: " \
    && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
    -d "dataId=${dataIdPrefix}trafficRouteRulePath&group=${groupId}&content=${idUnitMappingRule}" \
    && echo ""

  forbiddenRule=$(cat ./rule/forbiddenRuleEmpty.json)
  echo "$(date "+%Y-%m-%d %H:%M:%S") forbiddenRule 推送结果: " \
    && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
    -d "dataId=${dataIdPrefix}forbiddenRulePath&group=${groupId}&content=${forbiddenRule}" \
    && echo ""

  dataScopeRule=$(cat ./rule/dbProperty.json)
  echo "$(date "+%Y-%m-%d %H:%M:%S") dataScopeRule 推送结果: " \
    && curl -X POST "127.0.0.1:8848/nacos/v1/cs/configs" \
    -d "dataId=${dataIdPrefix}dataScopeRuleDirectoryPath_mysql-product&group=${groupId}&content=${dataScopeRule}" \
    && echo ""
fi

if [ `expr $type % 3` == 0 ]
then
  idSource=$(cat ./rule/idSource.json)
  idTransformer=$(cat ./rule/idTransformer.json)
  idUnitMapping=$(cat ./rule/idUnitMapping.json)

  gatewayRule="{\"idSource\" : $idSource, \"idTransformer\" : $idTransformer, \"idUnitMapping\" : $idUnitMapping}"
  data="{\"key\" : \"459236fc-ed71-4bc4-b46c-69fc60d31f18_test1122\", \"value\" : $gatewayRule}"
  echo $data
  echo "$(date "+%Y-%m-%d %H:%M:%S") gateway 基线推送结果: " && curl --header "Content-Type: application/json" \
  --request POST \
  --data "$data" \
  127.0.0.1:8090/set
fi



