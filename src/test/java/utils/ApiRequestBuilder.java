package utils;

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class ApiRequestBuilder {

    private final RequestSpecification spec;

    public ApiRequestBuilder(RequestSpecification spec) {
        this.spec = spec;
    }

    public RequestSpecification requestWithBody(Object body) {
        return given()
                .spec(spec)
                .body(body)
                .when()
                .log().all();
    }
}
