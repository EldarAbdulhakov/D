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

    public String getMetaValue(Integer userId, String key) {
        String query = """
                    SELECT meta_value
                    FROM wp_usermeta
                    WHERE user_id = ? AND meta_key = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setString(2, key);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("meta_value");
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Integer, Map<String, String>> getAllUserMeta() {
        String sql = "SELECT user_id, meta_key, meta_value FROM wp_usermeta";
        Map<Integer, Map<String, String>> allMeta = new HashMap<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String key = resultSet.getString("meta_key");
                String value = resultSet.getString("meta_value");

                allMeta.computeIfAbsent(userId, k -> new HashMap<>())
                        .put(key, value);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return allMeta;
    }
}
