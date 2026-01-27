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

    public void deletePost(int postId) {
        String query = "DELETE FROM wp_posts WHERE ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, postId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete post", e);
        }
    }
}
