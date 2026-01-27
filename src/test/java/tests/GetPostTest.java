package tests;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.*;

public class GetPostTest extends BaseTest {

    private int postId;
    private int userId;
    private final Integer INCORRECT_ID = 9999;

    @BeforeMethod
    public void createData() {
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
        postId = postDao.createPost(userId, "titleForIdUserMike", "contentForIdUserMike", "publish");
    }

    @Test
    public void testGetAllPosts() {
        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", hasItem(equalTo(postId)))
                .body("title.rendered", hasItem(equalTo("titleForIdUserMike")))
                .body("content.rendered", notNullValue())
                .body("author", hasItem(equalTo(userId)))
                .body("status", hasItem(equalTo("publish")));
    }

    @Test
    public void testGetLimitedPosts() {
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
        for (int i = 1; i < 7; i++) {
            postDao.createPost(userId,
                    "titleForIdUserMike%s".formatted(i),
                    "contentForIdUserMike%s".formatted(i),
                    "publish");
        }

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts&per_page=5")
                .then()
                .log().all()
                .statusCode(200)
                .body("size()", lessThanOrEqualTo(5));
    }

    @Test
    public void testPagination() {
        int postsByPage = 5;
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
        for (int i = 1; i < 7; i++) {
            postDao.createPost(userId,
                    "titleForIdUserMike%s".formatted(i),
                    "contentForIdUserMike%s".formatted(i),
                    "publish");
        }

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts&per_page=%s&page=2".formatted(postsByPage))
                .then()
                .log().all()
                .statusCode(200)
                .body("id", hasSize(postsByPage));
    }

    @Test
    public void testSearchPosts() {
        String searchText = "searchText";
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
        postId = postDao.createPost(
                userId,
                "titleForIdUserMike%s".formatted(searchText),
                "contentForIdUserMike",
                "publish");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts&search=%s".formatted(searchText))
                .then()
                .log().all()
                .statusCode(200)
                .body("id", hasItem(postId))
                .body(
                        "findAll { " +
                                "it.id == " + postId + " && (" +
                                "it.title.rendered.toLowerCase().contains('" + searchText.toLowerCase() + "') || " +
                                "it.content.rendered.toLowerCase().contains('" + searchText.toLowerCase() + "')" +
                                ") }.size()",
                        greaterThan(0)
                );
    }

    @Test
    public void testSortPostsAscending() throws InterruptedException {
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
        for (int i = 1; i < 6; i++) {
            postDao.createPost(userId,
                    "titleForIdUserMike%s".formatted(i),
                    "contentForIdUserMike%s".formatted(i),
                    "publish");
            sleep(1000);
        }

        List<String> dates = apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts&orderby=date&order=asc")
                .then()
                .log().all()
                .statusCode(200)
                .extract().path("date");

        boolean isSorted = IntStream.range(0, dates.size() - 1)
                .allMatch(i -> dates.get(i).compareTo(dates.get(i + 1)) <= 0);

        Assert.assertTrue(isSorted);
    }

    @Test
    public void testSortPostsDescending() throws InterruptedException {
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
        for (int i = 1; i < 6; i++) {
            postDao.createPost(userId,
                    "titleForIdUserMike%s".formatted(i),
                    "contentForIdUserMike%s".formatted(i),
                    "publish");
            sleep(1000);
        }

        List<String> dates = apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts&orderby=date&order=desc")
                .then()
                .log().all()
                .statusCode(200)
                .extract().path("date");

        boolean isSorted = IntStream.range(0, dates.size() - 1)
                .allMatch(i -> dates.get(i).compareTo(dates.get(i + 1)) >= 0);

        Assert.assertTrue(isSorted);
    }

    @Test
    public void testGetPostsByAuthor() {
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
        postId = postDao.createPost(userId, "titleForIdUserMike", "contentForIdUserMike", "publish");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts&author=%s".formatted(userId))
                .then()
                .log().all()
                .statusCode(200)
                .body("author", everyItem(equalTo(userId)));
    }

    @Test
    public void testGetPostById() {
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
        postId = postDao.createPost(userId, "titleForIdUserMike", "contentForIdUserMike", "publish");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts/%s".formatted(postId))
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(postId));
    }

    @Test
    public void testGetNonExistPost() {
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
        postId = postDao.createPost(userId, "titleForIdUserMike", "contentForIdUserMike", "publish");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts/%s".formatted(INCORRECT_ID))
                .then()
                .log().all()
                .statusCode(404)
                .body("code", equalTo("rest_post_invalid_id"))
                .body("message", equalTo("Неверный ID записи."))
                .body("data.status", equalTo(404));
    }
}
