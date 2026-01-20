package tests;

import io.restassured.http.ContentType;
import models.User;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import utils.BaseAPIRequests;
import utils.PropertyProvider;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CreateUserTest extends BaseTest {

    private Integer userId;

    @AfterMethod
    public void deleteUserAfterCreation(Method method) {
        if (method.getName().equals("testCreateUserWithoutUsername") ||
                method.getName().equals("testCreateUserWithoutEmail") ||
                method.getName().equals("testCreateUserWithoutPassword") ||
                method.getName().equals("testCreateUserIncorrectFormatEmail") ||
                method.getName().equals("testCreateUserWithoutAdministratorRights")) {
            return;
        }
        BaseAPIRequests.deleteUserById(userId, requestSpecification);
    }

    @Test
    public void testCreateUserWithRequiredFields() {
        User user = User.builder()
                .username("Mike")
                .email("mike456@gmail.com")
                .password("Dfgr44!")
                .build();

        userId = apiRequestBuilder
                .requestWithBody(user)
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(201)
                .extract().path("id");

        User dbUser = userDao.getDbUserById(userId);

        Assert.assertEquals(dbUser.getUsername(), user.getUsername());
        Assert.assertEquals(dbUser.getEmail(), user.getEmail());
    }

    @Test
    public void testCreateUserWithAllFields() {
        Map<String, Object> meta = new HashMap<>();
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("_modified", LocalDateTime.now()
                .format(DateTimeFormatter.ISO_DATE_TIME));
        meta.put("persisted_preferences", preferences);

        User user = User.builder()
                .username("Fulluser123")
                .email("fulluser@example.com")
                .password("SecurePassword123!")
                .name("John Michael Doe")
                .firstName("John")
                .lastName("Doe")
                .url("https://johndoe.example.com")
                .description("Experienced content writer and editor with 5+ years in digital media")
                .locale("ru_RU")
                .nickname("JD")
                .slug("full-user-slug")
                .roles(new ArrayList<>(List.of("editor")))
                .meta(meta)
                .build();

        userId = apiRequestBuilder
                .requestWithBody(user)
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(201)
                .extract().path("id");

        User dbUser = userDao.getDbUserById(userId);

        Assert.assertEquals(dbUser.getUsername(), user.getUsername());
        Assert.assertEquals(dbUser.getEmail(), user.getEmail());
        Assert.assertEquals(dbUser.getName(), user.getName());
        Assert.assertEquals(dbUser.getUrl(), user.getUrl());
        Assert.assertEquals(dbUser.getSlug(), user.getSlug());
        Assert.assertEquals(userMetaDao.getMetaValue(userId, "first_name"), user.getFirstName());
        Assert.assertEquals(userMetaDao.getMetaValue(userId, "last_name"), user.getLastName());
        Assert.assertEquals(userMetaDao.getMetaValue(userId, "locale"), user.getLocale());
        Assert.assertEquals(userMetaDao.getMetaValue(userId, "description"), user.getDescription());
        Assert.assertEquals(userMetaDao.getMetaValue(userId, "nickname"), user.getNickname());
    }

    @Test
    public void testCreateUserWithoutUsername() {
        User user = User.builder()
                .email("mike456@gmail.com")
                .password("Dfgr44!")
                .build();

        apiRequestBuilder
                .requestWithBody(user)
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(400);

        Assert.assertFalse(userDao.existsByEmail(user.getEmail()));
    }

    @Test
    public void testCreateUserWithoutEmail() {
        User user = User.builder()
                .username("Mike")
                .password("Dfgr44!")
                .build();

        apiRequestBuilder
                .requestWithBody(user)
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(400);

        Assert.assertFalse(userDao.existsByLogin(user.getUsername()));
    }

    @Test
    public void testCreateUserWithoutPassword() {
        User user = User.builder()
                .username("Mike")
                .email("mike456@gmail.com")
                .build();

        apiRequestBuilder
                .requestWithBody(user)
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(400);

        Assert.assertFalse(userDao.existsByLogin(user.getUsername()));
    }

    @Test
    public void testCreateUserIncorrectFormatEmail() {
        User user = User.builder()
                .username("Mike")
                .email("mike456.com")
                .password("Dfgr44!")
                .build();

        apiRequestBuilder
                .requestWithBody(user)
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(400);

        Assert.assertFalse(userDao.existsByLogin(user.getUsername()));
        Assert.assertFalse(userDao.existsByEmail(user.getEmail()));
    }

    @Test
    public void testCreateUserWithExistingUsername() {
        User user = User.builder()
                .username("Mike")
                .email("mike456@gmail.com")
                .password("Dfgr44!")
                .build();

        User userWithSameUsername = User.builder()
                .username("Mike")
                .email("ffgjgfgdfgdg@gmail.com")
                .password("OOOYYy4521!!!!!")
                .build();

        userId = apiRequestBuilder
                .requestWithBody(user)
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(201)
                .extract().path("id");

        apiRequestBuilder
                .requestWithBody(userWithSameUsername)
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(500);

        Assert.assertEquals(userDao.countByLogin(user.getUsername()), 1);
        Assert.assertFalse(userDao.existsByEmail(userWithSameUsername.getEmail()));
    }

    @Test
    public void testCreateUserWithExistingEmail() {
        User user = User.builder()
                .username("Mike")
                .email("mike456@gmail.com")
                .password("Dfgr44!")
                .build();

        User userWithSameEmail = User.builder()
                .username("Bob")
                .email("mike456@gmail.com")
                .password("GGGYYy4521!!!!!")
                .build();

        userId = apiRequestBuilder
                .requestWithBody(user)
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(201)
                .extract().path("id");

        apiRequestBuilder
                .requestWithBody(userWithSameEmail)
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(500);

        Assert.assertEquals(userDao.countByEmail(user.getEmail()), 1);
        Assert.assertFalse(userDao.existsByLogin(userWithSameEmail.getUsername()));
    }

    @Test
    public void testCreateUserWithoutAdministratorRights() {
        User user = User.builder()
                .username("Mike")
                .email("mike456@gmail.com")
                .password("Dfgr44!")
                .build();

        given()
                .contentType(ContentType.JSON)
                .baseUri(PropertyProvider.getInstance().getProperty("base.url"))
                .accept(ContentType.JSON)
                .body(user)
                .when()
                .log().all()
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .statusCode(401);

        Assert.assertFalse(userDao.existsByLogin(user.getUsername()));
    }
}
