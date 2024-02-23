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

package io.appactive.java.api.base.exception;

import io.appactive.java.api.base.exception.bo.IMsg;

/**
 * Exception factory
 */
public final class ExceptionFactory {

    private ExceptionFactory() {

    }

    public static AppactiveException makeFault(String msg) {
        return new AppactiveException("0000", msg);
    }

    public static AppactiveException makeFault(String msg, Throwable tr) {
        return new AppactiveException("0000", msg, tr);
    }

    /**
     * 直接构建异常
     *
     * @param code as it is
     * @param msg as it is
     * @return AppactiveException
     */
    public static AppactiveException makeFault(String code, String msg) {
        return new AppactiveException(code, msg);
    }

    /**
     * 直接构建异常（基于已存在的异常）
     *
     * @param code as it is
     * @param msg as it is
     * @param tr as it is
     * @return AppactiveException
     */
    public static AppactiveException makeFault(String code, String msg, Throwable tr) {
        return new AppactiveException(code, msg, tr);
    }

    /**
     * 创建业务异常
     *
     * @param messageInfo as it is
     * @param params as it is
     * @return AppactiveException
     */
    public static AppactiveException makeFault(IMsg messageInfo, Object... params) {
        String message = messageInfo.getMsg(params);
        return new AppactiveException(messageInfo.getKey(), message);
    }

    /**
     * 创建业务异常
     *
     * @param msg as it is
     * @return AppactiveException
     */
    public static AppactiveException makeFault(IMsg msg) {
        String message = msg.getMsg();
        return new AppactiveException(msg.getKey(), message);
    }

    /**
     * 创建具有异常对象的ServiceException
     *
     * @param msg as it is
     * @param tr as it is
     * @param params as it is
     * @return AppactiveException
     */
    public static AppactiveException makeFault(IMsg msg, Throwable tr, Object... params) {
        String message = msg.getMsg(params);
        return new AppactiveException(msg.getKey(), message, tr);
    }

}
