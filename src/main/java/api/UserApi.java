package api;

import io.restassured.response.Response;
import java.util.Map;

public class UserApi extends ApiClient {
    public Response getUsers() { return get("/users"); }
    public Response getUserById(int id) { return get("/users/" + id); }
    public Response createUser(Map<String, Object> data) { return post("/users", data); }
    public Response updateUser(int id, Map<String, Object> data) { return put("/users/" + id, data); }
    public Response deleteUser(int id) { return delete("/users/" + id); }
    public Response getUserPosts(int id) { return get("/users/" + id + "/posts"); }
}
