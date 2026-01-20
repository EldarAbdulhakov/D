package utils;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.User;

import static io.restassured.RestAssured.given;

public class BaseAPIRequests {

    public static RequestSpecification initRequestSpecification() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder
                .setContentType(ContentType.JSON)
                .setBaseUri(PropertyProvider.getInstance().getProperty("base.url"))
                .setAccept(ContentType.JSON)
                .setAuth(RestAssured.preemptive().basic(
                        PropertyProvider.getInstance().getProperty("admin.username"),
                        PropertyProvider.getInstance().getProperty("admin.password")));

        return requestSpecBuilder.build();
    }

    public static void deleteUserById(Integer userId, RequestSpecification requestSpecification) {
        given()
                .spec(requestSpecification)
                .when()
                .log().all()
                .delete("?rest_route=/wp/v2/users/%s&force=true&reassign=1".formatted(userId))
                .then()
                .log().all();
    }

    public static User CreateUser(RequestSpecification requestSpecification) {
        User user = User.builder()
                .username("Petr")
                .email("petra456@gmail.com")
                .password("Aqwed99!@#!")
                .firstName("PetrFirst")
                .lastName("PetrLast")
                .description("Petr description content writer and editor with 5+ years in digital media")
                .build();

        return given()
                .spec(requestSpecification)
                .body(user)
                .when()
                .log().all()
                .post("?rest_route=/wp/v2/users")
                .then()
                .log().all()
                .extract()
                .as(User.class);
    }
}
