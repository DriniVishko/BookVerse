
package com.bookverse.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory implements AutoCloseable {

    private final DatabaseConfig config;
    private final String engine;
    private final String mode;
    private HikariDataSource dataSource;

    public ConnectionFactory(DatabaseConfig config) {
        this.config = config;
        this.engine = config.get("database.engine").toUpperCase();
        this.mode = config.get("connection.mode").toUpperCase();

        if (!engine.equals("MYSQL") && !engine.equals("H2")) {
            throw new IllegalArgumentException(
                    "Motor no válido: " + engine
            );
        }

        if (!mode.equals("DRIVER_MANAGER") && !mode.equals("HIKARI")) {
            throw new IllegalArgumentException(
                    "Modo de conexión no válido: " + mode
            );
        }

        if (mode.equals("HIKARI")) {
            inicializarPool();
        }
    }

    private String url() {
        return engine.equals("MYSQL")
                ? config.get("mysql.url")
                : config.get("h2.url");
    }

    private String usuario() {
        return engine.equals("MYSQL")
                ? config.get("mysql.user")
                : config.get("h2.user");
    }

    private String password() {
        if (engine.equals("H2")) {
            return "";
        }

        String password = System.getenv("BOOKVERSE_DB_PASSWORD");

        if (password == null) {
            throw new IllegalStateException(
                    "Falta la variable BOOKVERSE_DB_PASSWORD"
            );
        }

        return password;
    }

    private void inicializarPool() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(url());
        hikariConfig.setUsername(usuario());
        hikariConfig.setPassword(password());
        hikariConfig.setMaximumPoolSize(
                config.getInt("hikari.maximumPoolSize")
        );
        hikariConfig.setPoolName("BookVersePool");

        dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        if (mode.equals("HIKARI")) {
            return dataSource.getConnection();
        }

        return DriverManager.getConnection(
                url(), usuario(), password()
        );
    }

    public String getEngine() {
        return engine;
    }

    public String getMode() {
        return mode;
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}

