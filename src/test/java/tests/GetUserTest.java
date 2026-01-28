package tests;

import io.restassured.http.ContentType;
import org.testng.annotations.Test;
import utils.PropertyProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetUserTest extends BaseTest {

    private Integer userId;
    private final Integer INCORRECT_ID = 9999;

    @Test
    public void testGetApiInfo() {
        given()
                .baseUri(PropertyProvider.getInstance().getProperty("base.url"))
                .accept(ContentType.JSON)
                .when()
                .log().all()
                .get("?rest_route=/")
                .then()
                .log().all()
                .statusCode(200)
                .body("name", equalTo("abdulhakov-site-probation"))
                .body("description", notNullValue())
                .body("url", equalTo("http://localhost:8000"))
                .body("namespaces", hasItems(
                        "oembed/1.0",
                        "wp/v2",
                        "wp-site-health/v1",
                        "wp-block-editor/v1",
                        "wp-abilities/v1"
                ))
                .body("routes", not(empty()))
                .body("routes.'/'", notNullValue())
                .body("routes.'/wp/v2'", notNullValue());
    }

    @Test
    public void testGetAllUsers() {
        userId = userDao.createUser("Bob", "bobs007@mail.com", "dfgdhf12$");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", hasItem(equalTo(userId)))
                .body("name", hasItem(equalTo(userDao.getNameById(userId))))
                .body("name", hasItems(userDao.getAllUserNames().toArray(new String[0])));
    }

    @Test
    public void testGetUserById() {
        userId = userDao.createUser("Bob", "bobs007@mail.com", "dfgdhf12$");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/users/%s".formatted(userId))
                .then()
                .log().all()
                .statusCode(200)
                .body("id", equalTo(userId))
                .body("name", equalTo(userDao.getNameById(userId)));
    }

    @Test
    public void testGetNonExistentUser() {
        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/users/%s".formatted(INCORRECT_ID))
                .then()
                .log().all()
                .statusCode(404)
                .body("code", equalTo("rest_user_invalid_id"))
                .body("message", equalTo("Неверный ID пользователя."))
                .body("data.status", equalTo(404));
    }

    @Test
    public void testSearchUserByName() {
        userId = userDao.createUser("Bob", "bobs007@mail.com", "dfgdhf12$");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/users&search=%s".formatted(userDao.getNameById(userId)))
                .then()
                .log().all()
                .statusCode(200)
                .body("name", everyItem(containsString(userDao.getNameById(userId))));
    }

    @Test
    public void testFilterUsersByAdminRole() {
        userId = userDao.createUser("Bob", "bobs007@mail.com", "dfgdhf12$");
        userMetaDao.upsertUserMeta(userId, "wp_capabilities", "a:1:{s:13:\"administrator\";b:1;}");
        userMetaDao.upsertUserMeta(userId, "wp_user_level", "10");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/users&roles=administrator")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", containsInAnyOrder(userMetaDao.getUserIdsByRole("administrator").toArray(new Integer[0])));
    }

    @Test
    public void testFilterUsersByEditorRole() {
        userId = userDao.createUser("Bob", "bobs007@mail.com", "dfgdhf12$");
        userMetaDao.upsertUserMeta(userId, "wp_capabilities", "a:1:{s:6:\"editor\";b:1;}");
        userMetaDao.upsertUserMeta(userId, "wp_user_level", "7");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/users&roles=editor")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", containsInAnyOrder(userMetaDao.getUserIdsByRole("editor").toArray(new Integer[0])));
    }

    @Test
    public void testFilterUsersBySubscriberRole() {
        userId = userDao.createUser("Bob", "bobs007@mail.com", "dfgdhf12$");
        userMetaDao.upsertUserMeta(userId, "wp_capabilities", "a:1:{s:10:\"subscriber\";b:1;}");
        userMetaDao.upsertUserMeta(userId, "wp_user_level", "0");

        apiRequestBuilder
                .request()
                .get("?rest_route=/wp/v2/users&roles=subscriber")
                .then()
                .log().all()
                .statusCode(200)
                .body("id", containsInAnyOrder(userMetaDao.getUserIdsByRole("subscriber").toArray(new Integer[0])));
    }

    @Test
    public void testGetUserByIdWithoutAdminRights() {
        userId = userDao.createUser("Bob", "bobs007@mail.com", "dfgdhf12$");

        given()
                .contentType(ContentType.JSON)
                .baseUri(PropertyProvider.getInstance().getProperty("base.url"))
                .accept(ContentType.JSON)
                .when()
                .log().all()
                .get("?rest_route=/wp/v2/users/%s".formatted(userId))
                .then()
                .log().all()
                .statusCode(401)
                .body("code", equalTo("rest_user_cannot_view"))
                .body("message", equalTo("Извините, вам не разрешено просматривать список пользователей."))
                .body("data.status", equalTo(401));
    }
}
