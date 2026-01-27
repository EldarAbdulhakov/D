package dao;

import models.User;
import utils.DataCleanRegistry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public User getDbUserById(Integer userId) {
        String query = "SELECT * FROM wp_users WHERE ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getIdByLogin(String login) {
        String query = "SELECT ID FROM wp_users WHERE user_login = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("ID");
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNameById(Integer id) {
        String query = "SELECT display_name FROM wp_users WHERE ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("display_name");
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsById(Integer id) {
        String query = "SELECT 1 FROM wp_users WHERE ID = ? LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsByLogin(String login) {
        return getIdByLogin(login) != null;
    }

    public boolean existsByEmail(String email) {
        String query = "SELECT 1 FROM wp_users WHERE user_email = ? LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countByLogin(String login) {
        String query = """
                    SELECT COUNT(*)
                    FROM wp_users
                    WHERE user_login = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countByEmail(String email) {
        String query = """
                    SELECT COUNT(*)
                    FROM wp_users
                    WHERE user_email = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> getAllUsers() {
        String sql = "SELECT * FROM wp_users";
        List<User> users = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return users;
    }

    private User mapResultSetToUser(ResultSet rs) {
        try {
            return User.builder()
                    .id(rs.getInt("ID"))
                    .username(rs.getString("user_login"))
                    .email(rs.getString("user_email"))
                    .name(rs.getString("display_name"))
                    .registeredDate(rs.getTimestamp("user_registered").toInstant().toString())
                    .url(rs.getString("user_url"))
                    .slug(rs.getString("user_nicename"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer createUser(String username, String email, String password) {
        String query = """
                INSERT INTO wp_users (user_login, user_nicename, user_pass, user_email, user_registered, display_name)
                VALUES (?, ?, MD5(?), ?, NOW(), ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, username.toLowerCase());
            statement.setString(3, password);
            statement.setString(4, email);
            statement.setString(5, username);

            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    Integer userId = resultSet.getInt(1);
                    DataCleanRegistry.addUser(userId);

                    return userId;
                }
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public List<String> getAllUserNames() {
        String query = "SELECT display_name FROM wp_users";
        List<String> names = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                names.add(resultSet.getString("display_name"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return names;
    }

    public void deleteUser(int userId) {
        String query = "DELETE FROM wp_users WHERE ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }
}
