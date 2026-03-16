package com.app.quantitymeasurement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionPool {

    private static ConnectionPool instance;

    private final String url;
    private final String user;
    private final String password;

    private ConnectionPool() {

        url = ApplicationConfig.get("db.url");
        user = ApplicationConfig.get("db.username");
        password = ApplicationConfig.get("db.password");

    }

    public static synchronized ConnectionPool getInstance() {

        if (instance == null)
            instance = new ConnectionPool();

        return instance;
    }

    public Connection getConnection() throws SQLException {

        return DriverManager.getConnection(url, user, password);

    }

}