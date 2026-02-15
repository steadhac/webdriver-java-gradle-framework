package api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class UserApiTest {

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
    }

    @Test
    public void getAllUsersReturns200() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/users")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }

    @Test
    public void getSingleUserReturns200() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/users/1")
        .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("name", notNullValue());
    }

    @Test
    public void createUserReturns201() {
        String body = """
                {
                    "name": "Test User",
                    "username": "testuser",
                    "email": "test@example.com"
                }
                """;

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("name", equalTo("Test User"));
    }
}
