package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserMetaDao {

    private final Connection connection;

    public UserMetaDao(Connection connection) {
        this.connection = connection;
    }

    public String getMetaValue(Integer userId, String key) throws SQLException {
        String query = """
                    SELECT meta_value
                    FROM wp_usermeta
                    WHERE user_id = ? AND meta_key = ?
                """;

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        statement.setString(2, key);

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("meta_value");
        }
        return null;
    }

    public Map<Integer, Map<String, String>> getAllUserMeta() throws SQLException {
        String sql = "SELECT user_id, meta_key, meta_value FROM wp_usermeta";
        Map<Integer, Map<String, String>> allMeta = new HashMap<>();

        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int userId = resultSet.getInt("user_id");
            String key = resultSet.getString("meta_key");
            String value = resultSet.getString("meta_value");

            allMeta.computeIfAbsent(userId, k -> new HashMap<>())
                    .put(key, value);
        }

        return allMeta;
    }
}
