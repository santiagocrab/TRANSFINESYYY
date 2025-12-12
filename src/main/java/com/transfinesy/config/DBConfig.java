package com.transfinesy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DBConfig implements ApplicationContextAware {

    private static volatile DataSource staticDataSource;
    private static volatile ApplicationContext applicationContext;
    private final DataSource dataSource;

    @Autowired
    public DBConfig(DataSource dataSource) {
        this.dataSource = dataSource;

        DBConfig.staticDataSource = dataSource;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {

        DBConfig.applicationContext = applicationContext;

        if (staticDataSource == null && applicationContext != null) {
            try {
                staticDataSource = applicationContext.getBean(DataSource.class);
            } catch (Exception e) {

            }
        }
    }

    public static Connection getConnection() throws SQLException {

        if (staticDataSource != null) {
            return staticDataSource.getConnection();
        }

        if (applicationContext != null) {
            try {
                staticDataSource = applicationContext.getBean(DataSource.class);
                if (staticDataSource != null) {
                    return staticDataSource.getConnection();
                }
            } catch (Exception e) {

            }
        }

        throw new IllegalStateException("DBConfig not initialized. Make sure Spring Boot has started and DBConfig bean is created. Error: DataSource is null.");
    }

    public Connection getConnectionInstance() throws SQLException {
        return dataSource.getConnection();
    }
}
