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

    public User getDbUserById(Integer userId) throws SQLException {
        String query = "SELECT * FROM wp_users WHERE ID = ?";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return mapResultSetToUser(resultSet);
        }
        return null;
    }

    public Integer getIdByLogin(String login) throws SQLException {
        String query = "SELECT ID FROM wp_users WHERE user_login = ?";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, login);

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("ID");
        }
        return null;
    }

    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM wp_users WHERE ID = ? LIMIT 1";

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);

        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    public boolean existsByLogin(String login) throws SQLException {
        return getIdByLogin(login) != null;
    }

    public boolean existsByEmail(String email) throws SQLException {
        String query = "SELECT 1 FROM wp_users WHERE user_email = ? LIMIT 1";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, email);

        ResultSet resultSet = statement.executeQuery();

        return resultSet.next();
    }

    public int countByLogin(String login) throws SQLException {
        String query = """
                    SELECT COUNT(*)
                    FROM wp_users
                    WHERE user_login = ?
                """;

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, login);

        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1);
    }

    public int countByEmail(String email) throws SQLException {
        String query = """
                    SELECT COUNT(*)
                    FROM wp_users
                    WHERE user_email = ?
                """;

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, email);

        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1);
    }

    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM wp_users";
        List<User> users = new ArrayList<>();

        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            users.add(mapResultSetToUser(resultSet));
        }

        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("ID"))
                .username(rs.getString("user_login"))
                .email(rs.getString("user_email"))
                .name(rs.getString("display_name"))
                .registeredDate(rs.getTimestamp("user_registered").toInstant().toString())
                .url(rs.getString("user_url"))
                .slug(rs.getString("user_nicename"))
                .build();
    }
}
