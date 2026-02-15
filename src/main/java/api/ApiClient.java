package api;

import config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Base HTTP client for all API testing in the framework.
 *
 * This class wraps REST Assured to provide a centralized, reusable HTTP client
 * with three core capabilities:
 *
 * - JWT Authentication — Login or manually set tokens; automatically
 *   attached to all subsequent requests via the Authorization header.
 * - Synchronous (SYNC) requests — Blocking HTTP calls that wait for
 *   the server response before returning. Execution pauses until the response
 *   is fully received.
 * - Asynchronous (ASYNC) requests — Non-blocking HTTP calls that run
 *   on a background thread pool. Returns a CompletableFuture immediately,
 *   allowing multiple requests to execute in parallel.
 *
 * Usage: Do not use this class directly. Extend it for each API domain
 * (e.g., UserApi) and expose named methods like getUsers(), createUser().
 * This keeps endpoint definitions separate from HTTP plumbing.
 *
 * Configuration: All requests use the base URL from TestConfig.API_BASE_URL,
 * send and accept JSON, and log full request/response details to the console.
 *
 * Example inheritance:
 *
 *   public class UserApi extends ApiClient {
 *       public Response getUsers() { return get("/users"); }
 *   }
 */
public class ApiClient {

    /**
     * The current JWT token. When set (non-null, non-empty), it is automatically
     * included as a Bearer token in the Authorization header of every request.
     * Set via authenticate() or setJwtToken(). Clear with setJwtToken(null).
     */
    private String jwtToken;

    /**
     * Fixed thread pool with 5 threads used for async HTTP calls.
     * Each async method submits work to this pool via CompletableFuture.supplyAsync().
     * Must be shut down via shutdown() in @AfterClass to prevent thread leaks.
     *
     * Why 5 threads: Provides enough concurrency for parallel API calls in tests
     * without overwhelming the target server or exhausting system resources.
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    // ═══════════════════════════════════════════════════════════════
    // JWT AUTHENTICATION
    // Methods for obtaining and managing JWT tokens.
    // Once a token is set, req() automatically attaches it to every request.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Authenticates against the API's login endpoint and stores the returned JWT.
     *
     * How it works:
     * 1. Sends a POST to /auth/login with JSON body: {"username": "...", "password": "..."}
     * 2. Extracts the "token" field from the JSON response
     * 3. Stores it in jwtToken so all future requests include it
     *
     * Example:
     *   api.authenticate("admin", "password123");
     *   // All subsequent calls now include: Authorization: Bearer <token>
     *   api.getUsers(); // authenticated request
     *
     * @param username the login username
     * @param password the login password
     * @return the JWT token string extracted from the login response
     */
    public String authenticate(String username, String password) {
        Response res = RestAssured.given()
                .baseUri(TestConfig.API_BASE_URL)
                .contentType(ContentType.JSON)
                .body(Map.of("username", username, "password", password))
                .post("/auth/login").then().extract().response();
        this.jwtToken = res.jsonPath().getString("token");
        return this.jwtToken;
    }

    /**
     * Manually sets a JWT token without calling the login endpoint.
     * Useful for testing with pre-generated tokens or clearing auth state.
     *
     * Example:
     *   api.setJwtToken("eyJhbGciOiJIUzI1NiJ9...");  // set token
     *   api.setJwtToken(null);                          // clear token (unauthenticated)
     *
     * @param token the JWT token string, or null to clear authentication
     */
    public void setJwtToken(String token) { this.jwtToken = token; }

    /**
     * Returns the currently stored JWT token.
     *
     * @return the JWT token, or null if no token is set
     */
    public String getJwtToken() { return this.jwtToken; }

    // ═══════════════════════════════════════════════════════════════
    // REQUEST BUILDER
    // Private helper that constructs a fully configured REST Assured
    // request specification. Every HTTP method calls this first.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Builds a pre-configured REST Assured RequestSpecification.
     *
     * Every request gets:
     * - baseUri from TestConfig.API_BASE_URL
     * - Content-Type: application/json — all requests send JSON
     * - Accept: application/json — all requests expect JSON responses
     * - Authorization: Bearer <token> — only if jwtToken is set
     * - Full request logging — prints method, URL, headers, and body to console
     *
     * This is the single point of configuration. To change headers, auth,
     * or logging for all requests, modify this method only.
     *
     * @return a configured request specification ready to send
     */
    private RequestSpecification req() {
        RequestSpecification spec = RestAssured.given()
                .baseUri(TestConfig.API_BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        if (jwtToken != null && !jwtToken.isEmpty())
            spec.header("Authorization", "Bearer " + jwtToken);
        return spec.log().all();
    }

    // ═══════════════════════════════════════════════════════════════
    // SYNCHRONOUS (SYNC) HTTP METHODS
    // These methods BLOCK — they wait for the server to respond before
    // returning. The calling thread is paused until the full response
    // (status code, headers, body) is received.
    //
    // Use these for standard sequential test flows where each step
    // depends on the previous one completing.
    //
    // Flow: req() → send HTTP request → wait → log response → return Response
    // ═══════════════════════════════════════════════════════════════

    /**
     * Sends a synchronous (blocking) GET request.
     *
     * Example: get("/users") sends GET to https://api.example.com/users
     *
     * @param ep the endpoint path, appended to the base URL (e.g., "/users", "/users/1")
     * @return the complete HTTP response including status code, headers, and body
     */
    public Response get(String ep) { return req().get(ep).then().log().all().extract().response(); }

    /**
     * Sends a synchronous (blocking) POST request with a JSON body.
     *
     * Example: post("/users", Map.of("name", "John"))
     * sends POST to https://api.example.com/users with body {"name":"John"}
     *
     * @param ep   the endpoint path
     * @param body the request body — can be a Map, POJO, or String (auto-serialized to JSON)
     * @return the complete HTTP response
     */
    public Response post(String ep, Object body) { return req().body(body).post(ep).then().log().all().extract().response(); }

    /**
     * Sends a synchronous (blocking) PUT request with a JSON body.
     * Typically used for full resource replacement.
     *
     * @param ep   the endpoint path (e.g., "/users/1")
     * @param body the full updated resource
     * @return the complete HTTP response
     */
    public Response put(String ep, Object body) { return req().body(body).put(ep).then().log().all().extract().response(); }

    /**
     * Sends a synchronous (blocking) PATCH request with a JSON body.
     * Typically used for partial resource updates.
     *
     * @param ep   the endpoint path (e.g., "/users/1")
     * @param body the fields to update
     * @return the complete HTTP response
     */
    public Response patch(String ep, Object body) { return req().body(body).patch(ep).then().log().all().extract().response(); }

    /**
     * Sends a synchronous (blocking) DELETE request.
     *
     * @param ep the endpoint path (e.g., "/users/1")
     * @return the complete HTTP response
     */
    public Response delete(String ep) { return req().delete(ep).then().log().all().extract().response(); }

    // ═══════════════════════════════════════════════════════════════
    // ASYNCHRONOUS (ASYNC) HTTP METHODS
    // These methods DO NOT BLOCK — they return a CompletableFuture
    // immediately and execute the HTTP call on a background thread
    // from the executor pool.
    //
    // Use these when you need to fire multiple requests in parallel
    // (e.g., fetching a user and their posts simultaneously) or when
    // you want to test concurrent API behavior.
    //
    // Flow: submit to thread pool → return Future → call .join() when
    //       you need the result (join blocks until complete)
    //
    // Example:
    //   CompletableFuture<Response> a = getAsync("/users/1");   // fires immediately
    //   CompletableFuture<Response> b = getAsync("/posts/1");   // fires immediately
    //   awaitAll(a, b);                                          // wait for both
    //   a.join().statusCode();                                   // get result
    // ═══════════════════════════════════════════════════════════════

    /**
     * Sends a GET request asynchronously on a background thread.
     *
     * @param ep the endpoint path
     * @return a CompletableFuture that completes with the response when the call finishes
     */
    public CompletableFuture<Response> getAsync(String ep) { return CompletableFuture.supplyAsync(() -> get(ep), executor); }

    /**
     * Sends a POST request asynchronously on a background thread.
     *
     * @param ep   the endpoint path
     * @param body the request body
     * @return a CompletableFuture that completes with the response
     */
    public CompletableFuture<Response> postAsync(String ep, Object body) { return CompletableFuture.supplyAsync(() -> post(ep, body), executor); }

    /**
     * Sends a DELETE request asynchronously on a background thread.
     *
     * @param ep the endpoint path
     * @return a CompletableFuture that completes with the response
     */
    public CompletableFuture<Response> deleteAsync(String ep) { return CompletableFuture.supplyAsync(() -> delete(ep), executor); }

    // ═══════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Blocks the current thread until ALL given futures have completed.
     * Use this after firing multiple async requests to wait for all results
     * before making assertions.
     *
     * Example:
     *   CompletableFuture<Response> a = getAsync("/users/1");
     *   CompletableFuture<Response> b = getAsync("/users/1/posts");
     *   awaitAll(a, b);  // blocks until both are done
     *
     * @param futures one or more futures to wait for
     */
    @SafeVarargs
    public final void awaitAll(CompletableFuture<Response>... futures) { CompletableFuture.allOf(futures).join(); }

    /**
     * Shuts down the async thread pool. Must be called in @AfterClass
     * to release threads and prevent resource leaks.
     *
     * After calling this, async methods will throw RejectedExecutionException.
     */
    public void shutdown() { executor.shutdown(); }
}