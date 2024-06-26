package ru.hogeltbellai.prison.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Getter
public class Database {

    public String jdbcUrl;
    public String username;
    public String password;
    public int poolMax;

    private final HikariDataSource hikariDataSource;

    public Database(String jdbcUrl, String username, String password, int poolMax) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.poolMax = poolMax;

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(poolMax);
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
            return true;
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

    public int queryUpdate(String sql, Object... par) {
        try (Connection connection = hikariDataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (int i = 0; i < par.length; i++) {
                    preparedStatement.setObject(i + 1, par[i]);
                }

                return preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public <T> T getVaule(String sql, Class<T> type, Object... params) {
        try(Connection connection = hikariDataSource.getConnection()) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        if (type == String.class) {
                            return type.cast(resultSet.getString(1));
                        } else if (type == Integer.class || type == int.class) {
                            return type.cast(resultSet.getInt(1));
                        } else if (type == BigDecimal.class) {
                            return type.cast(resultSet.getBigDecimal(1));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> getStringList(String sql, Object... params) {
        ArrayList<String> result = new ArrayList<>();
        try(Connection connection = hikariDataSource.getConnection()) {

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while(resultSet.next()) {
                        result.add(resultSet.getString(1));
                    }
                    return result;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
