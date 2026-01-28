package tests;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

public class GetCommentTest extends BaseTest {

    private int postId;
    private int userId;
    private int commentId;
    String authorName = "My author Name";
    String comment = "My comment by this post";

    @BeforeMethod
    public void createData() {
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
        postId = postDao.createPost(userId, "titleForUserMike", "contentForUserMike", "publish");
        commentId = commentDao.createComment(postId, authorName, comment);
    }

    @Test
    public void testGetAllComments() {
        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/comments")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", hasItem(equalTo(commentId)))
                .body("post", hasItem(equalTo(postId)))
                .body("author_name", hasItem(equalTo(authorName)))
                .body("content.rendered", hasItem(containsString(comment)));
    }

    @Test
    public void testFilterCommentsByPost() {
        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/comments&post=%s".formatted(postId))
                .then()
                .log().all()
                .statusCode(200)
                .body("id", hasItem(commentId))
                .body("content.rendered", hasItem(containsString(comment)));
    }
}
