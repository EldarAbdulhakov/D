package tests;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import utils.PropertyProvider;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class YandexDiskApiTest {

    private final PropertyProvider props = PropertyProvider.getInstance();

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = PropertyProvider.getInstance().getProperty("yandex.url");
    }

    @Test
    public void testGetWithValidToken() {
        given()
                .header("Authorization", "OAuth " + props.getProperty("yandex.auth.token"))
                .when()
                .log().all()
                .get("/v1/disk/")
                .then()
                .log().all()
                .statusCode(200)
                .body("user.login", equalTo(props.getProperty("yandex.login")))
                .body("user.display_name", equalTo(props.getProperty("yandex.display_name")));
    }

    @Test
    public void testGetWithoutToken() {
        given()
                .when()
                .log().all()
                .get("/v1/disk/")
                .then()
                .log().all()
                .statusCode(401)
                .body("error", notNullValue())
                .body("description", notNullValue())
                .body("message", notNullValue());
    }
}
