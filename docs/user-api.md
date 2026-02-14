# UserApi — Service Object

**File:** `src/main/java/api/UserApi.java`

## Purpose

`UserApi` is a thin service wrapper that extends `ApiClient` and maps each User REST endpoint to a named Java method. It contains no business logic — only endpoint routing.

## Class Diagram

ApiClient (base)
├── get(path) → Response
├── post(path, body) → Response
├── put(path, body) → Response
├── delete(path) → Response
├── getAsync(path) → CompletableFuture<Response>
├── postAsync(path, body) → CompletableFuture<Response>
├── awaitAll(futures...)
├── setJwtToken(token)
├── getJwtToken() → String
└── shutdown()
│
└── UserApi (extends ApiClient)
├── getUsers() → Response
├── getUserById(int id) → Response
├── createUser(Map data) → Response
├── updateUser(int id, Map data) → Response
├── deleteUser(int id) → Response
└── getUserPosts(int id) → Response


## Method Reference

| Method                          | HTTP Verb | Endpoint             | Parameters                    |
|---------------------------------|-----------|----------------------|-------------------------------|
| `getUsers()`                    | `GET`     | `/users`             | None                          |
| `getUserById(int id)`           | `GET`     | `/users/{id}`        | `id` — user ID                |
| `createUser(Map data)`          | `POST`    | `/users`             | `data` — JSON body as Map     |
| `updateUser(int id, Map data)`  | `PUT`     | `/users/{id}`        | `id` — user ID, `data` — body |
| `deleteUser(int id)`            | `DELETE`  | `/users/{id}`        | `id` — user ID                |
| `getUserPosts(int id)`          | `GET`     | `/users/{id}/posts`  | `id` — user ID                |

## Key Characteristics

- **No HTTP config** — Base URL, headers, content type, and auth are handled by `ApiClient`
- **Returns raw `Response`** — Callers (tests) receive `io.restassured.response.Response` for full flexibility
- **One-liner methods** — Each method is a single delegation call to the inherited `get()`/`post()`/`put()`/`delete()`
- **Extensible** — Add new endpoints by adding one method; no other files need to change

