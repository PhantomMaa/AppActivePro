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

cmd=$1

if [ "$cmd" == 'build' ]
then
  docker-compose build
fi

if [ "$cmd" == 'pre-start' ]
then
  docker-compose up -d nacos mysql
fi

if [ "$cmd" == 'start' ]
then
  cd ../appactive-portal || exit
  sh baseline.sh APP

  cd ../appactive-demo || exit
  docker-compose up -d

  sleep 10
  sh baseline.sh APP
fi

if [ "$cmd" == 'stop' ]
then
  docker-compose stop storage storage-unit product product-unit frontend frontend-unit gateway
fi

if [ "$cmd" == 'destroy' ]
then
  docker-compose down
fi
