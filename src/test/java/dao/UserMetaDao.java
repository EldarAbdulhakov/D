package dao;

import utils.DataCleanRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public List<Integer> getUserIdsByRole(String role) {
        String query = """
                    SELECT DISTINCT user_id
                    FROM wp_usermeta
                    WHERE meta_value
                    LIKE ?
                """;

        List<Integer> result = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + role + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getInt("user_id"));
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void upsertUserMeta(int userId, String key, String value) {
        String query = """
                INSERT INTO wp_usermeta (user_id, meta_key, meta_value)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE meta_value = VALUES(meta_value)
                """;

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setString(2, key);
            ps.setString(3, value);
            ps.executeUpdate();

            DataCleanRegistry.addUserMeta(userId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUserMeta(int userId) {
        String sql = "DELETE FROM wp_usermeta WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user meta", e);
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
