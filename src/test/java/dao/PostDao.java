package dao;

import utils.DataCleanRegistry;

import java.sql.*;

public class PostDao {

    private final Connection connection;

    public PostDao(Connection connection) {
        this.connection = connection;
    }

    public Integer createPost(int authorId, String title, String content, String status) {
        String query = """
                INSERT INTO wp_posts (post_author, post_date, post_date_gmt, post_content, post_title, post_excerpt,
                post_status, post_name, post_modified, post_modified_gmt, post_type, to_ping, pinged, post_content_filtered)
                VALUES (?, NOW(), UTC_TIMESTAMP(), ?, ?, '', ?, ?, NOW(), UTC_TIMESTAMP(), 'post', '', '', '')
                """;

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, authorId);
            statement.setString(2, content);
            statement.setString(3, title);
            statement.setString(4, status);
            statement.setString(5, title.toLowerCase().replace(" ", "-"));

            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    Integer postId = resultSet.getInt(1);
                    DataCleanRegistry.addPost(postId);

                    return postId;
                }
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create post", e);
        }
    }

    public void createPostWithDateOffset(int userId, String title, String content, String status, int offsetSeconds) {
        String query = """
                INSERT INTO wp_posts (post_author, post_date, post_date_gmt, post_content, post_title, post_excerpt,
                post_status, post_name, post_modified, post_modified_gmt, post_type, to_ping, pinged, post_content_filtered)
                VALUES (?, DATE_ADD(NOW(), INTERVAL ? SECOND), DATE_ADD(UTC_TIMESTAMP(), INTERVAL ? SECOND), ?, ?, '', ?, ?,
                DATE_ADD(NOW(), INTERVAL ? SECOND), DATE_ADD(UTC_TIMESTAMP(), INTERVAL ? SECOND), 'post', '', '', '')
                """;

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, userId);
            statement.setInt(2, offsetSeconds);
            statement.setInt(3, offsetSeconds);
            statement.setString(4, content);
            statement.setString(5, title);
            statement.setString(6, status);
            statement.setString(7, title.toLowerCase().replace(" ", "-"));
            statement.setInt(8, offsetSeconds);
            statement.setInt(9, offsetSeconds);

            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    Integer postId = resultSet.getInt(1);
                    DataCleanRegistry.addPost(postId);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create posts", e);
        }
    }

    public void deletePost(int postId) {
        String query = "DELETE FROM wp_posts WHERE ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, postId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete post", e);
        }
    }

    public void createPosts(int postsCount, int userId, String title, String content, String status) {
        for (int i = 1; i <= postsCount; i++) {
            createPost(userId, title + i, content + i, status);
        }
    }

    public void createPostsWithDiffDate(int postsCount, int userId, String title, String content, String status) {
        for (int i = 1; i <= postsCount; i++) {
            createPostWithDateOffset(userId, title + i, content + i, status, i);
        }
    }
}
