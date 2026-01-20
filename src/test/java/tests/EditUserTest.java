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
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class EditUserTest extends BaseTest {

    private User user;
    private final Integer INCORRECT_ID = 9999;

    @BeforeMethod
    public void createUserForEdit() {
        user = BaseAPIRequests.CreateUser(requestSpecification);
    }

    @AfterMethod
    public void deleteUserAfterCreation(Method method) {
        BaseAPIRequests.deleteUserById(user.getId(), requestSpecification);
    }

    @Test
    public void testEditFirstName() {
        String newFirstName = "editingName";

        User userForBody = User.builder()
                .firstName(newFirstName)
                .build();

        given()
                .spec(requestSpecification)
                .body(userForBody)
                .when()
                .log().all()
                .patch("?rest_route=/wp/v2/users/%s".formatted(user.getId()))
                .then()
                .log().all()
                .statusCode(200);

        Assert.assertEquals(userMetaDao.getMetaValue(user.getId(), "first_name"), newFirstName);
    }

    @Test
    public void testEditEmail() {
        String newEmail = "editingEmail@gmail.com";

        User userForBody = User.builder()
                .email(newEmail)
                .build();

        given()
                .spec(requestSpecification)
                .body(userForBody)
                .when()
                .log().all()
                .patch("?rest_route=/wp/v2/users/%s".formatted(user.getId()))
                .then()
                .log().all()
                .statusCode(200);

        Assert.assertEquals(userDao.getDbUserById(user.getId()).getEmail(), newEmail);
    }

    @Test
    public void testEditMultipleFields() {
        String newFirstName = "editfirstname";
        String newLastName = "editlastname";
        String newDescription = "editdescription";

        User userForBody = User.builder()
                .firstName(newFirstName)
                .lastName(newLastName)
                .description(newDescription)
                .build();

        given()
                .spec(requestSpecification)
                .body(userForBody)
                .when()
                .log().all()
                .patch("?rest_route=/wp/v2/users/%s".formatted(user.getId()))
                .then()
                .log().all()
                .statusCode(200);

        Assert.assertEquals(userMetaDao.getMetaValue(user.getId(), "first_name"), newFirstName);
        Assert.assertEquals(userMetaDao.getMetaValue(user.getId(), "last_name"), newLastName);
        Assert.assertEquals(userMetaDao.getMetaValue(user.getId(), "description"), newDescription);
    }

    @Test
    public void testEditNameNonExistentUser() {
        List<User> usersBefore = userDao.getAllUsers();
        Map<Integer, Map<String, String>> userMetaBefore = userMetaDao.getAllUserMeta();

        User userForBody = User.builder()
                .name("editingName")
                .build();

        given()
                .spec(requestSpecification)
                .body(userForBody)
                .when()
                .log().all()
                .patch("?rest_route=/wp/v2/users/%s".formatted(INCORRECT_ID))
                .then()
                .log().all()
                .statusCode(404);

        List<User> usersAfter = userDao.getAllUsers();
        Map<Integer, Map<String, String>> userMetaAfter = userMetaDao.getAllUserMeta();

        Assert.assertFalse(userDao.existsById(INCORRECT_ID));
        Assert.assertEquals(usersAfter, usersBefore);
        Assert.assertEquals(userMetaAfter, userMetaBefore);
    }

    @Test
    public void testEditOnIncorrectFormatEmail() {
        String newIncorrectFormatEmail = "editingEmail.com";

        User userForBody = User.builder()
                .email(newIncorrectFormatEmail)
                .build();

        given()
                .spec(requestSpecification)
                .body(userForBody)
                .when()
                .log().all()
                .patch("?rest_route=/wp/v2/users/%s".formatted(user.getId()))
                .then()
                .log().all()
                .statusCode(400);

        Assert.assertNotEquals(userDao.getDbUserById(user.getId()).getEmail(), newIncorrectFormatEmail);
        Assert.assertNotNull(userDao.getDbUserById(user.getId()));
    }

    @Test
    public void testEditNameWithoutAdministratorRights() {
        List<User> usersBefore = userDao.getAllUsers();
        Map<Integer, Map<String, String>> userMetaBefore = userMetaDao.getAllUserMeta();

        User userForBody = User.builder()
                .name("editingName")
                .build();

        given()
                .contentType(ContentType.JSON)
                .baseUri(PropertyProvider.getInstance().getProperty("base.url"))
                .accept(ContentType.JSON)
                .body(userForBody)
                .when()
                .log().all()
                .patch("?rest_route=/wp/v2/users/%s".formatted(user.getId()))
                .then()
                .log().all()
                .statusCode(401);

        List<User> usersAfter = userDao.getAllUsers();
        Map<Integer, Map<String, String>> userMetaAfter = userMetaDao.getAllUserMeta();

        Assert.assertEquals(usersAfter, usersBefore);
        Assert.assertEquals(userMetaAfter, userMetaBefore);
    }
}
