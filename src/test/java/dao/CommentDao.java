package dao;

import utils.DataCleanRegistry;

import java.sql.*;

public class CommentDao {

    private final Connection connection;

    public CommentDao(Connection connection) {
        this.connection = connection;
    }

    public int createComment(int postId, String authorName, String content) {
        String sql = """
                INSERT INTO wp_comments
                (comment_post_ID, comment_author, comment_author_email, comment_author_url, comment_date,
                comment_date_gmt, comment_content, comment_approved, comment_type)
                VALUES (?, ?, ?, '', NOW(), UTC_TIMESTAMP(), ?, '1', 'comment')
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, postId);
            statement.setString(2, authorName);
            statement.setString(3, authorName.toLowerCase().replace(" ", ".") + "@example.com");
            statement.setString(4, content);

            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int commentId = resultSet.getInt(1);
                    DataCleanRegistry.addComment(commentId);

                    return commentId;
                }
            }
            return -1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create comment", e);
        }
    }

    public void deleteComment(int commentId) {
        String sql = "DELETE FROM wp_comments WHERE comment_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, commentId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete comment", e);
        }
    }
}
