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

package io.appactive.db.mysql.driver;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.*;
import java.util.Properties;

import io.appactive.db.mysql.utils.SQLCacheCheckUtil;
import io.appactive.java.api.bridge.db.connection.MysqlConnectionService;
import io.appactive.java.api.bridge.db.driver.MysqlDriverService;
import io.appactive.support.log.LogUtil;
import io.appactive.support.spi.SpiUtil;
import org.slf4j.Logger;

/**
 * <pre>
 * how to use？:Class.forName("io.appactive.db.mysql.driver.Driver")
 * </pre>
 */
public class Driver implements java.sql.Driver, MysqlDriverService {

    private static final Logger logger = LogUtil.getLogger();

    private static final String MYSQL_DRIVER_NAME_5 = "com.mysql.jdbc.NonRegisteringDriver";

    private static final String MYSQL_DRIVER_NAME_8 = "com.mysql.cj.jdbc.NonRegisteringDriver";

    private static final String OCEAN_BASE_DRIVER_NAME = "com.alipay.oceanbase.jdbc.NonRegisteringDriver";

    private static final Driver APPACTIVE_DRIVER = new Driver();

    private static final java.sql.Driver PROXY_DRIVER = getCurrentDriver();

    private static final MysqlConnectionService mysqlConnectionService;

    static {
        // init connection
        mysqlConnectionService = SpiUtil.loadFirstInstance(MysqlConnectionService.class);
        // register driver
        registerDriver();
    }

    private static void registerDriver() {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                DriverManager.registerDriver(APPACTIVE_DRIVER);
            } catch (SQLException e) {
                throw new RuntimeException("Can't register driver!", e);
            }
            return null;
        });
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return PROXY_DRIVER.acceptsURL(url);
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        // 初始化多活信息
        mysqlConnectionService.initDriverConnect(url,info);
        check(info);
        Connection conn = PROXY_DRIVER.connect(url, info);
        if (conn == null) {
            return null;
        }
        return mysqlConnectionService.getConnection(conn, info);
    }

    @Override
    public int getMajorVersion() {
        return getProxyDriver().getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return getProxyDriver().getMajorVersion();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return PROXY_DRIVER.getPropertyInfo(url, info);
    }

    @Override
    public boolean jdbcCompliant() {
        return getProxyDriver().jdbcCompliant();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return PROXY_DRIVER.getParentLogger();
    }

    @Override
    public java.sql.Driver getProxyDriver() {
        return getCurrentDriver();
    }

    private static java.sql.Driver getCurrentDriver() {
        // 先找ob的driver
        java.sql.Driver realDriver = classForNameDriver(OCEAN_BASE_DRIVER_NAME);
        if (realDriver != null) {
            return realDriver;
        }
        // 先找8的 find driver and instance
        realDriver = classForNameDriver(MYSQL_DRIVER_NAME_8);
        if (realDriver != null) {
            return realDriver;
        }
        // 再找5的
        realDriver = classForNameDriver(MYSQL_DRIVER_NAME_5);
        if(realDriver != null){
            return realDriver;
        }
        logger.error("Driver getDriver fail. Can't find any driver class, only support OceanBase MySQL5 and MySQL8");
        throw new RuntimeException(
            "Driver getDriver fail. Can't find any driver class, only support OceanBase MySQL5 and MySQL8");
    }

    private static java.sql.Driver classForNameDriver(String driverName) {
        // find driver and instance
        try {
            return (java.sql.Driver)Class.forName(driverName).newInstance();
        } catch (Throwable e) {
            // do nothing
        }
        return null;
    }

    @Override
    public void check(Properties info) {
        boolean dbAppactive = mysqlConnectionService.isInAppActive(info);
        if (!dbAppactive){
            return;
        }
        SQLCacheCheckUtil.checkDruidSqlCache();
    }

}
