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

package io.appactive.support.thread;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeWrappers {

    private final static Logger logger = LoggerFactory.getLogger(SafeWrappers.class);

    /**
     * 封装 runnable，防止异常不被捕获
     *
     * @param runnable as it is
     * @return runnable
     */
    public static Runnable safeRunnable(final Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                logger.error("SafeWrappers-safeRunnable-error" + e.getMessage(), e);
            }
        };
    }
}
