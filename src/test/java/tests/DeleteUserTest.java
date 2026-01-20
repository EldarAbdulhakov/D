package tests;

import io.restassured.http.ContentType;
import models.User;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.BaseAPIRequests;
import utils.PropertyProvider;

import java.lang.reflect.Method;

import static io.restassured.RestAssured.given;

public class DeleteUserTest extends BaseTest {

    private User user;
    private final Integer INCORRECT_ID = 9999;

    @BeforeMethod
    public void createUserForDelete() {
        user = BaseAPIRequests.CreateUser(requestSpecification);
    }

    @AfterMethod
    public void deleteUserAfterCreation(Method method) {
        if (method.getName().equals("testDeleteUser")) {
            return;
        }
        BaseAPIRequests.deleteUserById(user.getId(), requestSpecification);
    }

    @Test
    public void testDeleteUser() {
        Assert.assertTrue(userDao.existsByLogin(user.getUsername()));
        Assert.assertTrue(userDao.existsById(user.getId()));

        apiRequestBuilder
                .request()
                .delete("?rest_route=/wp/v2/users/%s&force=true&reassign=1".formatted(user.getId()))
                .then()
                .log().all()
                .statusCode(200);

        Assert.assertFalse(userDao.existsById(user.getId()));
        Assert.assertFalse(userDao.existsByLogin(user.getUsername()));
    }

    @Test
    public void testDeleteUserWithoutForce() {
        apiRequestBuilder
                .request()
                .delete("?rest_route=/wp/v2/users/%s&reassign=1".formatted(user.getId()))
                .then()
                .log().all()
                .statusCode(501);

        Assert.assertTrue(userDao.existsById(user.getId()));
        Assert.assertEquals(
                userDao.getDbUserById(user.getId()).getUsername(),
                user.getUsername());
    }

    @Test
    public void testDeleteUserWithoutReassign() {
        apiRequestBuilder
                .request()
                .delete("?rest_route=/wp/v2/users/%s&force=true".formatted(user.getId()))
                .then()
                .log().all()
                .statusCode(400);

        Assert.assertTrue(userDao.existsById(user.getId()));
        Assert.assertEquals(
                userDao.getDbUserById(user.getId()).getUsername(),
                user.getUsername());
    }

    @Test
    public void testDeleteUserWithIncorrectReassign() {
        apiRequestBuilder
                .request()
                .delete("?rest_route=/wp/v2/users/%s&force=true&reassign=%s".formatted(user.getId(), INCORRECT_ID))
                .then()
                .log().all()
                .statusCode(400);

        Assert.assertTrue(userDao.existsById(user.getId()));
        Assert.assertEquals(
                userDao.getDbUserById(user.getId()).getUsername(),
                user.getUsername());
    }

    @Test
    public void testDeleteNonExistentUser() {
        apiRequestBuilder
                .request()
                .delete("?rest_route=/wp/v2/users/%s&force=true&reassign=1".formatted(INCORRECT_ID))
                .then()
                .log().all()
                .statusCode(404);

        Assert.assertTrue(userDao.existsById(user.getId()));
        Assert.assertEquals(
                userDao.getDbUserById(user.getId()).getUsername(),
                user.getUsername());
    }

    @Test
    public void testDeleteUserWithoutAdministratorRights() {
        given()
                .contentType(ContentType.JSON)
                .baseUri(PropertyProvider.getInstance().getProperty("base.url"))
                .accept(ContentType.JSON)
                .when()
                .log().all()
                .delete("?rest_route=/wp/v2/users/%s&force=true&reassign=1".formatted(user.getId()))
                .then()
                .log().all()
                .statusCode(401);

        Assert.assertTrue(userDao.existsById(user.getId()));
        Assert.assertEquals(
                userDao.getDbUserById(user.getId()).getUsername(),
                user.getUsername());
    }
}
