package dao;

import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
