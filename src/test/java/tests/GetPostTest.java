package tests;

import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;

public class GetPostTest extends BaseTest {

    private int postId;
    private int userId;
    private final Integer INCORRECT_ID = 9999;

    @BeforeMethod
    public void createData() {
        userId = userDao.createUser("Mike", "bobs007@mail.com", "dfgdhf12$");
    }

    @Test
    public void testGetAllPosts() {
        postId = postDao.createPost(userId, "titleForIdUserMike", "contentForIdUserMike", "publish");

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
        int postsByPage = 5;
        postDao.createPosts(6, userId, "titleForIdUserMike", "contentForIdUserMike", "publish");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts&per_page=%s".formatted(postsByPage))
                .then()
                .log().all()
                .statusCode(200)
                .body("size()", lessThanOrEqualTo(postsByPage));
    }

    @Test
    public void testPagination() {
        int postsByPage = 5;
        postDao.createPosts(6, userId, "titleForIdUserMike", "contentForIdUserMike", "publish");

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
        postId = postDao.createPost(
                userId,
                "titleForIdUserMike%s".formatted(searchText),
                "contentForIdUserMike",
                "publish");

        JsonPath jsonPath = apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/posts&search=%s".formatted(searchText))
                .then()
                .log().all()
                .statusCode(200)
                .body("id", hasItem(postId))
                .extract().jsonPath();

        List<String> titles = jsonPath.getList("title.rendered");
        List<String> contents = jsonPath.getList("content.rendered");

        boolean foundInTitle =
                titles != null && titles.stream().anyMatch(t -> t.contains(searchText));

        boolean foundInContent =
                contents != null && contents.stream().anyMatch(c -> c.contains(searchText));

        Assert.assertTrue(
                foundInTitle || foundInContent, "searchText not found in title OR content");
    }

    @Test
    public void testSortPostsAscending() {
        postDao.createPostsWithDiffDate(6, userId, "titleDiffDate", "contentDiffDate", "publish");

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
    public void testSortPostsDescending() {
        postDao.createPostsWithDiffDate(6, userId, "titleDiffDate", "contentDiffDate", "publish");

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
