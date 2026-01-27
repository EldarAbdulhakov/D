package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    PropertyProvider.getInstance().getProperty("db.url"),
                    PropertyProvider.getInstance().getProperty("db.username"),
                    PropertyProvider.getInstance().getProperty("db.password")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
}
