package api;

import config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import java.util.concurrent.*;

public class ApiClient {
    private String jwtToken;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // ── JWT ──
    public String authenticate(String username, String password) {
        Response res = RestAssured.given()
                .baseUri(TestConfig.API_BASE_URL)
                .contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .post("/auth/login").then().extract().response();
        this.jwtToken = res.jsonPath().getString("token");
        return this.jwtToken;
    }

    public void setJwtToken(String token) { this.jwtToken = token; }
    public String getJwtToken() { return this.jwtToken; }

    private RequestSpecification req() {
        RequestSpecification spec = RestAssured.given()
                .baseUri(TestConfig.API_BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        if (jwtToken != null && !jwtToken.isEmpty())
            spec.header("Authorization", "Bearer " + jwtToken);
        return spec.log().all();
    }

    // ── SYNC ──
    public Response get(String ep) { return req().get(ep).then().log().all().extract().response(); }
    public Response post(String ep, Object body) { return req().body(body).post(ep).then().log().all().extract().response(); }
    public Response put(String ep, Object body) { return req().body(body).put(ep).then().log().all().extract().response(); }
    public Response patch(String ep, Object body) { return req().body(body).patch(ep).then().log().all().extract().response(); }
    public Response delete(String ep) { return req().delete(ep).then().log().all().extract().response(); }

    // ── ASYNC ──
    public CompletableFuture<Response> getAsync(String ep) { return CompletableFuture.supplyAsync(() -> get(ep), executor); }
    public CompletableFuture<Response> postAsync(String ep, Object body) { return CompletableFuture.supplyAsync(() -> post(ep, body), executor); }
    public CompletableFuture<Response> deleteAsync(String ep) { return CompletableFuture.supplyAsync(() -> delete(ep), executor); }

    @SafeVarargs
    public final void awaitAll(CompletableFuture<Response>... futures) { CompletableFuture.allOf(futures).join(); }
    public void shutdown() { executor.shutdown(); }
}
