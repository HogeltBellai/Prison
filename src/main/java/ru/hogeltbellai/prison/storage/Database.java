package ru.hogeltbellai.prison.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Getter
public class Database {

    public String jdbcUrl;
    public String username;
    public String password;

    private final HikariDataSource hikariDataSource;

    public Database(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setLeakDetectionThreshold(10000);

        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public void connection() {
        isConnected();
    }

    public void disconnect() {
        if(hikariDataSource != null) {
            hikariDataSource.close();
        }
    }

    public Connection getConnection() {
        try {
            hikariDataSource.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean isConnected() {
        try {
            hikariDataSource.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void query(String sql, Object... par) {
        try(Connection connection = hikariDataSource.getConnection()) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (int i = 0; i < par.length; i++) {
                    preparedStatement.setObject(i + 1, par[i]);
                }

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
