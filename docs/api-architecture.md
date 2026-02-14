# API Testing Architecture

## Overview

This framework uses a **layered Service Object pattern** for API testing, separating concerns across three layers:

```
┌──────────────────────────────────────────────────────────────────────┐
│  Test Layer (UserApiTest.java)        ← Assertions only, no HTTP    │
├──────────────────────────────────────────────────────────────────────┤
│  Service Layer (UserApi.java)         ← Endpoint definitions        │
├──────────────────────────────────────────────────────────────────────┤
│  HTTP Client Layer (ApiClient.java)   ← REST Assured, auth, async   │
└──────────────────────────────────────────────────────────────────────┘
```



## Design Principles

- **Single Responsibility** — Tests only assert. Service objects only define endpoints. The base client handles HTTP mechanics.
- **Encapsulation** — REST Assured is used internally but never exposed directly in tests. Tests interact with domain-named methods like `getUsers()`.
- **Reusability** — `ApiClient` can be extended for any API (`UserApi`, `PostApi`, `CommentApi`, etc.) without duplicating HTTP logic.
- **Maintainability** — If an endpoint changes, only the corresponding service class is updated — not every test.

## Comparison: This Pattern vs Direct REST Assured

| Aspect                  | This Framework (Service Object)       | Direct REST Assured (`given/when/then`) |
|-------------------------|---------------------------------------|-----------------------------------------|
| HTTP details in tests   | Hidden behind `UserApi`               | Visible in every test                   |
| Reusability             | High — shared service classes         | Low — copy/paste across tests           |
| Async support           | Built into `ApiClient`                | Manual `CompletableFuture` wrapping     |
| State management (JWT)  | Managed by `ApiClient` instance       | Manual per-request headers              |
| Readability             | Domain-focused (`api.getUsers()`)     | HTTP-focused (`get("/users")`)          |
| Maintenance on change   | Update one method in service class    | Update every test referencing endpoint  |
| Setup/teardown          | Lifecycle managed (`shutdown()`)      | No lifecycle needed                     |

## When to Use Each

### Use this pattern when:
- Multiple test classes consume the same API
- Async/parallel calls are needed
- Stateful sessions (JWT, cookies) must be managed
- You want to swap the HTTP library without touching tests
- The API client may be reused outside of tests

### Use direct REST Assured when:
- Small project with < 10 tests
- One-off or exploratory API validation
- No shared state or reuse is needed