# UserApiTest — Test Documentation

**File:** `src/test/java/api/UserApiTest.java`

## Overview

TestNG test class that validates the User REST API through the `UserApi` service object. Includes synchronous CRUD tests, asynchronous parallel tests, and JWT authentication tests. All tests are annotated with Allure reporting metadata.

## Allure Reporting Structure
```
API Testing
└── Feature: User API
    ├── Story: Get Users (CRITICAL)
    ├── Story: Get User (CRITICAL)
    ├── Story: Create User (CRITICAL)
    ├── Story: Update User (NORMAL)
    ├── Story: Delete User (NORMAL)
    ├── Story: Async (NORMAL)
    ├── Story: Async Create (NORMAL)
    └── Story: JWT (CRITICAL)
```

## Lifecycle

| Annotation     | Method       | Purpose                                           |
|----------------|--------------|---------------------------------------------------|
| `@BeforeClass` | `setUp()`    | Creates a `UserApi` instance (HTTP client + pool)  |
| `@AfterClass`  | `tearDown()`  | Calls `api.shutdown()` to release resources        |

## Test Reference

### Synchronous Tests

| Test              | Endpoint         | Assertions                                          | Severity |
|-------------------|------------------|-----------------------------------------------------|----------|
| `testGetUsers`    | `GET /users`     | Status 200; response array is non-empty             | CRITICAL |
| `testGetUserById` | `GET /users/1`   | Status 200; `id` field equals 1                     | CRITICAL |
| `testCreateUser`  | `POST /users`    | Status 201; `name` matches "Test User"              | CRITICAL |
| `testUpdateUser`  | `PUT /users/1`   | Status 200; `name` matches "Updated"                | NORMAL   |
| `testDeleteUser`  | `DELETE /users/1`| Status 200                                          | NORMAL   |

### Asynchronous Tests

| Test                | What It Does                                                        | Severity |
|---------------------|---------------------------------------------------------------------|----------|
| `testAsyncParallel` | Fires `GET /users/1` and `GET /users/1/posts` in parallel, asserts both return 200 | NORMAL |
| `testAsyncCreate`   | Creates a user asynchronously, asserts 201                          | NORMAL   |

**How async works:**
1. `api.getAsync(path)` returns a `CompletableFuture<Response>`
2. `api.awaitAll(futures...)` blocks until all complete
3. `.join()` retrieves the result for assertion

### JWT Test

| Test       | What It Does                                                                  | Severity |
|------------|-------------------------------------------------------------------------------|----------|
| `testJwt`  | Sets a JWT token, verifies it's stored, makes an authenticated call, clears it | CRITICAL |

**Flow:**
1. `api.setJwtToken("eyJ...")` — stores token on the client instance
2. `api.getJwtToken().startsWith("eyJ")` — confirms storage
3. `api.getUsers()` — sends request with `Authorization: Bearer <token>` header
4. `api.setJwtToken(null)` — clears token to reset state for other tests

