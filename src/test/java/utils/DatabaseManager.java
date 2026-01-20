package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PropertyProvider.getInstance().getProperty("db.url"),
                PropertyProvider.getInstance().getProperty("db.username"),
                PropertyProvider.getInstance().getProperty("db.password"));
    }
}
