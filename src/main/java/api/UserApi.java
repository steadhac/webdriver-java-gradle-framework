package api;

import java.util.Map;

import io.restassured.response.Response;

/**
 * Service object for the User API endpoints.
 *
 * Extends ApiClient to inherit HTTP methods, authentication, and async support.
 * Each method maps a domain action to a REST endpoint — no logic, just routing.
 *
 * If the User API changes (e.g., /users becomes /api/v2/users), update
 * the paths here and all tests automatically pick up the change.
 *
 * Example usage in tests:
 *
 *   UserApi api = new UserApi();
 *   Response r = api.getUserById(1);
 *   Assert.assertEquals(r.statusCode(), 200);
 */
public class UserApi extends ApiClient {

    /**
     * Fetches all users.
     * GET /users
     *
     * @return response containing a JSON array of all user objects
     */
    public Response getUsers() { return get("/users"); }

    /**
     * Fetches a single user by their ID.
     * GET /users/{id}
     *
     * @param id the user ID (e.g., 1, 2, 3)
     * @return response containing a single user JSON object
     */
    public Response getUserById(int id) { return get("/users/" + id); }

    /**
     * Creates a new user.
     * POST /users
     *
     * @param data the user fields as key-value pairs (e.g., "name", "username", "email")
     * @return response with status 201 and the created user (including assigned ID)
     */
    public Response createUser(Map<String, Object> data) { return post("/users", data); }

    /**
     * Replaces an existing user's data entirely.
     * PUT /users/{id}
     *
     * @param id   the user ID to update
     * @param data the full updated user fields
     * @return response with status 200 and the updated user
     */
    public Response updateUser(int id, Map<String, Object> data) { return put("/users/" + id, data); }

    /**
     * Deletes a user by their ID.
     * DELETE /users/{id}
     *
     * @param id the user ID to delete
     * @return response with status 200 on success
     */
    public Response deleteUser(int id) { return delete("/users/" + id); }

    /**
     * Fetches all posts authored by a specific user.
     * GET /users/{id}/posts
     *
     * @param id the user ID whose posts to retrieve
     * @return response containing a JSON array of post objects
     */
    public Response getUserPosts(int id) { return get("/users/" + id + "/posts"); }
}