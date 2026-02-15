

Here's the fully rewritten document using one continuous story — building and testing an e-commerce platform called **ShopEasy** — that threads through all 50 questions:

```markdown
# Top 50 Interview Questions — API, UI, Java, Gradle & WebDriver

All answers follow one continuous story: building and testing **ShopEasy**, an e-commerce platform with a REST API and a web storefront.

---

## The Story

You've joined ShopEasy as an SDET. The platform has:
- A **REST API** at `https://api.shopeasy.com` for users, products, orders, and payments
- A **web UI** at `https://shopeasy.com` with login, product catalog, cart, and checkout
- A **Gradle** build system with **TestNG** and **Allure** reporting

Your job: build the test automation framework from scratch.

---

## Part 1: API Testing (Questions 1–15)

*You start by testing the ShopEasy API before the UI is even built.*

---

### 1. What is REST and what are its key principles?

ShopEasy's API follows REST. When you browse products, your browser makes requests like:

```
GET  https://api.shopeasy.com/products          → list all products
GET  https://api.shopeasy.com/products/42        → get product #42
POST https://api.shopeasy.com/orders             → create a new order
PUT  https://api.shopeasy.com/orders/100         → replace order #100
DELETE https://api.shopeasy.com/orders/100       → cancel order #100
```

REST principles in action:
- **Stateless** — each request includes the JWT token; the server doesn't remember you between calls
- **Client-Server** — the React frontend and the Java backend are separate deployments
- **Uniform Interface** — every resource uses the same HTTP verbs (GET, POST, PUT, DELETE)
- **Resource-Based** — `/products/42` identifies a specific product
- **Cacheable** — product listings are cached; order data is not

---

### 2. What is the difference between PUT and PATCH?

A customer changes their shipping address. Should the frontend use PUT or PATCH?

```java
// PUT — replaces the ENTIRE order. You must send everything, even unchanged fields.
// If you forget to include "items", they'll be wiped out.
Response r = api.put("/orders/100", Map.of(
    "items", List.of(Map.of("productId", 42, "qty", 2)),
    "shipping", Map.of("address", "456 New St", "city", "Austin"),
    "payment", "visa-ending-4242"
));

// PATCH — updates ONLY the fields you send. Everything else stays the same.
Response r = api.patch("/orders/100", Map.of(
    "shipping", Map.of("address", "456 New St", "city", "Austin")
));
```

**Rule of thumb:** Use PUT when replacing a resource entirely. Use PATCH when changing a few fields.

---

### 3. What HTTP status codes should you know?

You test every ShopEasy endpoint and encounter these:

```java
// Happy paths
api.get("/products");                    // 200 OK — products listed
api.post("/orders", validOrder);         // 201 Created — order placed
api.delete("/cart/items/5");             // 204 No Content — item removed, empty response

// Client errors (your fault)
api.get("/products/99999");              // 404 Not Found — product doesn't exist
api.post("/orders", Map.of());           // 400 Bad Request — missing required fields
api.get("/orders/100");                  // 401 Unauthorized — no JWT token
api.delete("/admin/users/1");            // 403 Forbidden — you're not an admin
api.patch("/products/42", updateData);   // 405 Method Not Allowed — customers can't edit products

// Server errors (their fault)
api.post("/checkout", hugeOrder);        // 500 Internal Server Error — payment service crashed
```

| Code | Meaning | ShopEasy Example |
|------|---------|------------------|
| 200 | OK | `GET /products` returns product list |
| 201 | Created | `POST /orders` creates a new order |
| 204 | No Content | `DELETE /cart/items/5` removes item silently |
| 400 | Bad Request | `POST /orders` with empty body |
| 401 | Unauthorized | `GET /orders` without a JWT token |
| 403 | Forbidden | Non-admin tries `DELETE /admin/users/1` |
| 404 | Not Found | `GET /products/99999` — doesn't exist |
| 405 | Method Not Allowed | `PATCH /products/42` — not supported |
| 429 | Too Many Requests | Rate-limited after 100 requests/minute |
| 500 | Internal Server Error | Payment gateway timeout |

---

### 4. What is REST Assured and why use it?

You choose REST Assured as ShopEasy's API testing library. Here's your first test — verifying the product catalog:

```java
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

// Direct REST Assured — BDD-style
given()
    .baseUri("https://api.shopeasy.com")
    .header("Authorization", "Bearer " + token)
.when()
    .get("/products")
.then()
    .statusCode(200)
    .body("$", hasSize(greaterThan(0)))
    .body("[0].name", notNullValue())
    .body("[0].price", greaterThan(0.0f));
```

REST Assured gives you:
- **Fluent DSL** — `given/when/then` reads like English
- **Built-in JsonPath** — `.body("name", equalTo("Laptop"))` without manual parsing
- **Logging** — `.log().all()` prints the full request and response
- **Auth helpers** — `.auth().oauth2(token)` instead of manual headers

---

### 5. What is the difference between given/when/then (DSL) vs the Service Object pattern?

Your team decides the direct DSL is too verbose for 200+ tests. You build a `ProductApi` wrapper:

```java
// Direct REST Assured — tests are coupled to HTTP details
@Test
public void testGetProduct() {
    given()
        .baseUri("https://api.shopeasy.com")
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
    .when()
        .get("/products/42")
    .then()
        .statusCode(200)
        .body("name", equalTo("Laptop"));
}

// Service Object pattern — tests use domain language
@Test
public void testGetProduct() {
    Response r = productApi.getProduct(42);
    Assert.assertEquals(r.statusCode(), 200);
    Assert.assertEquals(r.jsonPath().getString("name"), "Laptop");
}
```

Behind the scenes, `ProductApi` wraps the REST Assured calls:
```java
public class ProductApi extends ApiClient {
    public Response getProduct(int id) { return get("/products/" + id); }
    public Response listProducts()     { return get("/products"); }
    public Response createProduct(Map<String, Object> data) { return post("/products", data); }
}
```

**When to use each:**
- DSL directly → small project, < 20 tests, one person
- Service Object → team project, 50+ tests, shared across modules, maintainability matters

---

### 6. How do you handle authentication in API tests?

ShopEasy uses JWT. Your `ApiClient` handles the full auth lifecycle:

```java
// Option 1: Login to get a token
api.authenticate("customer@shopeasy.com", "password123");
// Internally: POST /auth/login → extracts "token" from response → stores it
// All subsequent calls include: Authorization: Bearer eyJhbG...

// Option 2: Set a pre-generated token directly (for testing with specific roles)
api.setJwtToken("eyJhbGciOiJIUzI1NiJ9.admin-token.sig");

// Option 3: Clear the token to test unauthorized access
api.setJwtToken(null);
Response r = api.get("/orders");  // should return 401

// How it works inside ApiClient.req():
private RequestSpecification req() {
    RequestSpecification spec = RestAssured.given()
        .baseUri(TestConfig.API_BASE_URL)
        .contentType(ContentType.JSON);
    if (jwtToken != null && !jwtToken.isEmpty())
        spec.header("Authorization", "Bearer " + jwtToken);  // auto-attached
    return spec;
}
```

Other auth methods you might encounter:
```java
// Basic Auth
given().auth().basic("username", "password").when().get("/admin");

// OAuth 2.0
given().auth().oauth2(accessToken).when().get("/profile");

// API Key as header
given().header("X-API-Key", "abc123").when().get("/data");

// API Key as query param
given().queryParam("api_key", "abc123").when().get("/data");
```

---

### 7. What is the difference between synchronous and asynchronous API calls?

A ShopEasy customer opens their order page. The page needs: order details, shipping status, and payment history. These are three independent API calls.

**Sync — one at a time (slow):**
```java
// Total time: 300ms + 200ms + 250ms = 750ms
Response order    = api.get("/orders/100");       // waits 300ms
Response shipping = api.get("/shipping/100");     // waits 200ms (starts AFTER order finishes)
Response payments = api.get("/payments?order=100"); // waits 250ms (starts AFTER shipping)
```

```
order    ████████████░░░░░░░░░░░░░░░        300ms
shipping ░░░░░░░░░░░░████████░░░░░░░        200ms
payments ░░░░░░░░░░░░░░░░░░░░██████████    250ms
Total:   ═══════════════════════════════    750ms
```

**Async — all at once (fast):**
```java
// Total time: max(300ms, 200ms, 250ms) = 300ms
CompletableFuture<Response> order    = api.getAsync("/orders/100");
CompletableFuture<Response> shipping = api.getAsync("/shipping/100");
CompletableFuture<Response> payments = api.getAsync("/payments?order=100");
api.awaitAll(order, shipping, payments);  // wait for ALL three

Assert.assertEquals(order.join().statusCode(), 200);
Assert.assertEquals(shipping.join().jsonPath().getString("status"), "delivered");
```

```
order    ████████████                       300ms
shipping ████████                           200ms  (runs at the same time)
payments ██████████                         250ms  (runs at the same time)
Total:   ════════════                       300ms
```

**When to use sync vs async:**

```java
// SYNC — step 2 depends on step 1's result
Response cart = api.post("/cart", Map.of("productId", 42));    // need cart ID
int cartId = cart.jsonPath().getInt("id");
Response checkout = api.post("/checkout", Map.of("cartId", cartId)); // uses cart ID

// ASYNC — all independent, no data dependencies
CompletableFuture<Response> products = api.getAsync("/products");
CompletableFuture<Response> categories = api.getAsync("/categories");
CompletableFuture<Response> deals = api.getAsync("/deals");
api.awaitAll(products, categories, deals);
```

---

### 8. How do you validate a JSON response body?

ShopEasy's `GET /products/42` returns:
```json
{
    "id": 42,
    "name": "Wireless Headphones",
    "price": 79.99,
    "category": "Electronics",
    "tags": ["audio", "wireless", "bluetooth"],
    "seller": {
        "name": "TechStore",
        "rating": 4.8
    }
}
```

Validating with JsonPath:
```java
Response r = productApi.getProduct(42);

// Simple fields
Assert.assertEquals(r.jsonPath().getInt("id"), 42);
Assert.assertEquals(r.jsonPath().getString("name"), "Wireless Headphones");
Assert.assertEquals(r.jsonPath().getDouble("price"), 79.99);

// Nested object
Assert.assertEquals(r.jsonPath().getString("seller.name"), "TechStore");
Assert.assertTrue(r.jsonPath().getFloat("seller.rating") > 4.0);

// Array
List<String> tags = r.jsonPath().getList("tags");
Assert.assertTrue(tags.contains("bluetooth"));
Assert.assertEquals(tags.size(), 3);
```

Validating with Hamcrest matchers (DSL style):
```java
given().when().get("/products/42").then()
    .body("id", equalTo(42))
    .body("name", equalTo("Wireless Headphones"))
    .body("price", greaterThan(0.0f))
    .body("tags", hasSize(3))
    .body("tags", hasItem("bluetooth"))
    .body("seller.name", equalTo("TechStore"))
    .body("seller.rating", greaterThanOrEqualTo(4.0f));
```

---

### 9. What is API contract testing?

The ShopEasy frontend team and backend team agree on a contract for `GET /products/{id}`:

```json
{
    "id":       "integer, required",
    "name":     "string, required, max 200 chars",
    "price":    "number, required, > 0",
    "category": "string, required, enum: [Electronics, Clothing, Books, Home]",
    "tags":     "array of strings, optional"
}
```

Contract test — verify the response matches the agreed schema:
```java
@Test
public void testProductContractMatchesSchema() {
    Response r = productApi.getProduct(42);

    // Required fields exist and have correct types
    Assert.assertNotNull(r.jsonPath().get("id"));
    Assert.assertNotNull(r.jsonPath().get("name"));
    Assert.assertNotNull(r.jsonPath().get("price"));
    Assert.assertNotNull(r.jsonPath().get("category"));

    // Type checks
    Assert.assertTrue(r.jsonPath().getInt("id") > 0);
    Assert.assertTrue(r.jsonPath().getString("name").length() <= 200);
    Assert.assertTrue(r.jsonPath().getDouble("price") > 0);

    // Enum validation
    List<String> validCategories = List.of("Electronics", "Clothing", "Books", "Home");
    Assert.assertTrue(validCategories.contains(r.jsonPath().getString("category")));
}
```

If the backend team adds a required field or changes a type without telling you, this test catches it.

---

### 10. How do you handle test data in API tests?

ShopEasy tests need products, users, and orders. Here are all the strategies:

```java
// 1. Hardcoded Map — quick and simple
Response r = api.post("/products", Map.of(
    "name", "Test Headphones",
    "price", 49.99,
    "category", "Electronics"
));

// 2. JSON file — complex data, shared across tests
// src/test/resources/testData.json:
// { "validProduct": { "name": "Laptop", "price": 999.99, "category": "Electronics" } }
JsonObject product = DataHelper.loadTestData("testData.json").getAsJsonObject("validProduct");

// 3. Random data — unique per run, avoids duplicate conflicts
Map<String, Object> uniqueUser = Map.of(
    "name", "Test_" + DataHelper.randomString(6),
    "email", DataHelper.randomEmail(),     // e.g., "test_k3mf9x@test.com"
    "username", "user_" + DataHelper.randomString(4)
);

// 4. POJO — type-safe, auto-serialized to JSON
Product product = new Product("Laptop", 999.99, "Electronics");
Response r = api.post("/products", product);  // REST Assured serializes it

// 5. DataProvider — run the same test with multiple data sets
@DataProvider
public Object[][] productData() {
    return new Object[][] {
        {"Laptop", 999.99, "Electronics"},
        {"Novel", 14.99, "Books"},
        {"T-Shirt", 24.99, "Clothing"}
    };
}

@Test(dataProvider = "productData")
public void testCreateProduct(String name, double price, String category) {
    Response r = api.post("/products", Map.of("name", name, "price", price, "category", category));
    Assert.assertEquals(r.statusCode(), 201);
}
```

---

### 11. What is idempotency and which HTTP methods are idempotent?

A ShopEasy customer's browser is flaky. They accidentally click "Place Order" three times. What happens?

```java
// POST is NOT idempotent — three clicks = three orders!
api.post("/orders", orderData);  // order #101 created
api.post("/orders", orderData);  // order #102 created (duplicate!)
api.post("/orders", orderData);  // order #103 created (another duplicate!)

// PUT IS idempotent — three calls = same result
api.put("/orders/101", updatedData);  // order #101 updated
api.put("/orders/101", updatedData);  // same update, no change
api.put("/orders/101", updatedData);  // still the same

// GET IS idempotent — reading never changes data
api.get("/products/42");  // returns product
api.get("/products/42");  // same product, nothing changed

// DELETE IS idempotent — deleting twice = still deleted
api.delete("/orders/101");  // order deleted
api.delete("/orders/101");  // already gone, 404 or 200 — but state is the same
```

| Method | Idempotent? | ShopEasy Impact |
|--------|-------------|-----------------|
| GET | Yes | Browsing products doesn't change inventory |
| PUT | Yes | Updating shipping address twice = same address |
| DELETE | Yes | Cancelling an already-cancelled order = still cancelled |
| POST | **No** | Clicking "Place Order" twice = two orders (bug!) |
| PATCH | **No** | Incrementing a counter: `{"views": "+1"}` changes each time |

---

### 12. How do you test negative API scenarios?

The happy path works. Now break things on purpose:

```java
// 404 — Product doesn't exist
@Test
public void testProductNotFound() {
    Response r = productApi.getProduct(99999);
    Assert.assertEquals(r.statusCode(), 404);
    Assert.assertEquals(r.jsonPath().getString("error"), "Product not found");
}

// 400 — Missing required fields
@Test
public void testCreateProductMissingName() {
    Response r = api.post("/products", Map.of("price", 9.99));  // no "name"
    Assert.assertEquals(r.statusCode(), 400);
    Assert.assertTrue(r.jsonPath().getString("message").contains("name is required"));
}

// 401 — No authentication
@Test
public void testUnauthorizedAccess() {
    api.setJwtToken(null);
    Response r = api.get("/orders");
    Assert.assertEquals(r.statusCode(), 401);
}

// 403 — Wrong role
@Test
public void testCustomerCannotDeleteProducts() {
    api.authenticate("customer@shopeasy.com", "pass123");  // customer role
    Response r = api.delete("/products/42");
    Assert.assertEquals(r.statusCode(), 403);
}

// 422 — Invalid data format
@Test
public void testInvalidPrice() {
    Response r = api.post("/products", Map.of("name", "Laptop", "price", -50));
    Assert.assertEquals(r.statusCode(), 422);
    Assert.assertTrue(r.jsonPath().getString("error").contains("price must be positive"));
}
```

---

### 13. What is the difference between API testing and integration testing?

```
┌────────────────────────────────────────────────────────────────────┐
│ API Test: "Does POST /orders return 201 with the right body?"      │
│ Scope: One endpoint, mocked dependencies                          │
│ Speed: ~50ms per test                                              │
│                                                                    │
│   Test → POST /orders → API Server → (mocked DB) → 201 response   │
└────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────┐
│ Integration Test: "Does placing an order update the database,      │
│   send an email, and charge the credit card?"                      │
│ Scope: Multiple components, real dependencies                      │
│ Speed: ~2-5 seconds per test                                       │
│                                                                    │
│   Test → POST /orders → API → Real DB → Email Service → Payment   │
└────────────────────────────────────────────────────────────────────┘
```

For ShopEasy, you write both:
```java
// API test — fast, isolated
@Test
public void testCreateOrderReturns201() {
    Response r = api.post("/orders", validOrder);
    Assert.assertEquals(r.statusCode(), 201);  // just checks the HTTP response
}

// Integration test — slow, full flow
@Test
public void testOrderFlowEndToEnd() {
    Response order = api.post("/orders", validOrder);
    int orderId = order.jsonPath().getInt("id");

    // Verify the database was updated
    Response dbCheck = api.get("/orders/" + orderId);
    Assert.assertEquals(dbCheck.jsonPath().getString("status"), "pending");

    // Verify the payment was charged
    Response payment = api.get("/payments?orderId=" + orderId);
    Assert.assertEquals(payment.jsonPath().getString("status"), "charged");
}
```

---

### 14. How do you handle rate limiting in API tests?

ShopEasy's API allows 100 requests per minute. Your test suite has 200 tests.

```java
// Problem — too many requests
for (int i = 0; i < 200; i++) {
    Response r = api.get("/products/" + i);
    // After request #100: 429 Too Many Requests
}

// Solution 1: Simple delay (easy but slow)
@AfterMethod
public void throttle() throws InterruptedException {
    Thread.sleep(700);  // 700ms between tests ≈ 85 requests/minute
}

// Solution 2: Retry with exponential backoff (production-grade)
public Response getWithRetry(String endpoint) {
    int retries = 3;
    long waitMs = 1000;
    for (int i = 0; i < retries; i++) {
        Response r = api.get(endpoint);
        if (r.statusCode() != 429) return r;
        try { Thread.sleep(waitMs); } catch (InterruptedException e) { break; }
        waitMs *= 2;  // 1s → 2s → 4s
    }
    throw new RuntimeException("Rate limited after " + retries + " retries");
}

// Solution 3: Check the Retry-After header
Response r = api.get("/products");
if (r.statusCode() == 429) {
    int waitSeconds = Integer.parseInt(r.header("Retry-After"));
    Thread.sleep(waitSeconds * 1000L);
    r = api.get("/products");  // retry
}
```

---

### 15. What is request/response logging and why is it important?

A ShopEasy test fails: `Expected 201 but got 400`. Without logging, you're guessing. With logging, you see exactly what happened:

```
// Request logging output:
Request method: POST
Request URI:    https://api.shopeasy.com/orders
Headers:        Authorization=Bearer eyJhbG...
                Content-Type=application/json
Body:
{
    "productId": 42,
    "quantity": 2
}

// Response logging output:
HTTP/1.1 400 Bad Request
Headers:        Content-Type=application/json
Body:
{
    "error": "Missing required field: shipping_address"
}
```

Now you know! The test forgot to include `shipping_address`.

This framework logs automatically in both directions:
```java
// Request logging — in req()
return spec.log().all();

// Response logging — in every HTTP method
public Response get(String ep) {
    return req().get(ep)
        .then().log().all()        // logs status, headers, body
        .extract().response();
}
```

---

## Part 2: Selenium WebDriver (Questions 16–30)

*The ShopEasy UI is ready. Time to automate browser tests.*

---

### 16. What is the Page Object Model (POM) and why use it?

You need to test the ShopEasy login flow. Without POM, tests are messy:

```java
// BAD — Selenium calls scattered in the test
@Test
public void testLogin() {
    driver.get("https://shopeasy.com/login");
    driver.findElement(By.id("email")).clear();
    driver.findElement(By.id("email")).sendKeys("customer@shopeasy.com");
    driver.findElement(By.id("password")).clear();
    driver.findElement(By.id("password")).sendKeys("pass123");
    driver.findElement(By.cssSelector("button[type='submit']")).click();
    Assert.assertTrue(driver.findElement(By.tagName("h2")).getText().contains("Dashboard"));
}
// If the ID changes from "email" to "user-email", you update EVERY test that logs in.
```

With POM, you create a `LoginPage` class:
```java
public class LoginPage extends BasePage {
    private final By inputEmail    = By.id("email");
    private final By inputPassword = By.id("password");
    private final By btnSubmit     = By.cssSelector("button[type='submit']");

    public LoginPage(WebDriver driver) { super(driver); }

    public void open() { super.open("/login"); }

    public void login(String email, String password) {
        type(inputEmail, email);
        type(inputPassword, password);
        click(btnSubmit);
    }
}

// Test reads like a user story
@Test
public void testLogin() {
    loginPage.open();
    loginPage.login("customer@shopeasy.com", "pass123");
    Assert.assertTrue(dashboardPage.isLoaded());
}
// If the ID changes, update ONE line in LoginPage. Zero test changes.
```

---

### 17. What are the three Selenium wait strategies?

ShopEasy's checkout page has a loading spinner. After clicking "Place Order", the payment processes for 2-5 seconds, then a confirmation appears.

**Implicit Wait — global, simple, imprecise:**
```java
// Sets a 10-second timeout on ALL findElement calls globally
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

// Now every findElement will wait up to 10s before throwing
driver.findElement(By.id("confirmation"));  // waits up to 10s
driver.findElement(By.id("order-number"));  // also waits up to 10s
// Problem: you can't set different timeouts for different elements
```

**Explicit Wait — per-element, condition-based (recommended):**
```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

// Wait for the spinner to disappear
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("spinner")));

// Wait for the confirmation to be visible
WebElement confirmation = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("confirmation"))
);
Assert.assertTrue(confirmation.getText().contains("Order confirmed"));

// Wait for the download button to be clickable
wait.until(ExpectedConditions.elementToBeClickable(By.id("download-receipt"))).click();
```

**Fluent Wait — explicit + custom polling + exception handling:**
```java
// ShopEasy's order status updates via WebSocket at unpredictable intervals
FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(30))       // max wait: 30 seconds
    .pollingEvery(Duration.ofSeconds(2))        // check every 2 seconds (not default 500ms)
    .ignoring(NoSuchElementException.class)     // don't crash if element isn't there yet
    .ignoring(StaleElementReferenceException.class);  // handle DOM re-renders

WebElement status = fluentWait.until(d -> {
    WebElement el = d.findElement(By.id("order-status"));
    return el.getText().equals("Shipped") ? el : null;  // return null = keep polling
});
```

**When to use each:**
| Scenario | Strategy |
|----------|----------|
| Simple page, elements always present | Implicit (or none) |
| Wait for a button to be clickable | Explicit |
| Wait for a spinner to disappear | Explicit |
| Poll for a status change every 2s | Fluent |
| Element re-renders cause stale references | Fluent (ignoring StaleElementReferenceException) |

---

### 18. Why should you avoid mixing implicit and explicit waits?

```java
// Implicit wait set to 10 seconds
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

// Explicit wait set to 5 seconds — should timeout in 5s, right?
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nonexistent")));

// WRONG — this might wait up to 15 SECONDS because:
// 1. Explicit wait polls every 500ms, calling findElement each time
// 2. Each findElement call waits up to 10s (implicit wait)
// 3. Total = unpredictable, potentially 10s + 5s = 15s
```

**Fix:** Always use explicit waits and keep implicit at 0:
```java
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
// Now waits exactly up to 10 seconds, polling every 500ms with no hidden delays
```

---

### 19. What is ThreadLocal and why does DriverManager use it?

ShopEasy runs 4 login tests in parallel. Without ThreadLocal:
```java
// BAD — shared static driver
private static WebDriver driver;  // all 4 threads share ONE browser

// Thread 1: driver.get("https://shopeasy.com/login")
// Thread 2: driver.get("https://shopeasy.com/products")  ← overwrites Thread 1!
// Thread 1: driver.findElement(By.id("email"))  ← CRASH: we're on products page now
```

With ThreadLocal — each thread gets its own browser:
```java
private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

// Thread 1: driver.set(new ChromeDriver())  → browser A
// Thread 2: driver.set(new ChromeDriver())  → browser B
// Thread 3: driver.set(new ChromeDriver())  → browser C
// Thread 4: driver.set(new ChromeDriver())  → browser D
// Each thread operates on its own browser — no interference
```

---

### 20. What is the difference between findElement and findElements?

ShopEasy's product page shows a list of reviews. Some products have reviews, some don't.

```java
// findElement — throws if not found
driver.findElement(By.cssSelector(".review"));  // NoSuchElementException if no reviews!

// findElements — returns empty list if not found (safe)
List<WebElement> reviews = driver.findElements(By.cssSelector(".review"));
if (reviews.isEmpty()) {
    System.out.println("No reviews yet");
} else {
    System.out.println("Found " + reviews.size() + " reviews");
    String firstReview = reviews.get(0).getText();
}

// Common pattern: check if a popup exists before acting
boolean hasPopup = driver.findElements(By.id("promo-popup")).size() > 0;
if (hasPopup) {
    driver.findElement(By.id("close-popup")).click();
}
```

---

### 21. What locator strategies does Selenium support?

Finding the "Add to Cart" button on ShopEasy's product page:

```java
// By.id — fastest, most reliable (if the element has a unique ID)
driver.findElement(By.id("add-to-cart-42"));

// By.name — good for form fields
driver.findElement(By.name("quantity"));

// By.cssSelector — versatile, fast, handles complex selectors
driver.findElement(By.cssSelector("button.add-to-cart[data-product='42']"));
driver.findElement(By.cssSelector("#product-42 .price"));
driver.findElement(By.cssSelector("input[placeholder='Search products']"));

// By.xpath — powerful but slower, handles text matching
driver.findElement(By.xpath("//button[text()='Add to Cart']"));
driver.findElement(By.xpath("//div[@class='product']//span[@class='price']"));
driver.findElement(By.xpath("//h3[contains(text(), 'Headphones')]/following-sibling::button"));

// By.className — single CSS class
driver.findElement(By.className("product-card"));

// By.tagName — when there's only one
driver.findElement(By.tagName("h1"));  // product title

// By.linkText / By.partialLinkText — anchor tags only
driver.findElement(By.linkText("View all reviews"));
driver.findElement(By.partialLinkText("reviews"));
```

**Preference order:** `id` > `cssSelector` > `xpath` > others

---

### 22. How do you handle dynamic elements?

ShopEasy generates product IDs dynamically: `product-abc123`, `product-def456`. The suffix changes each page load.

```java
// CSS selector with partial match
driver.findElement(By.cssSelector("[id^='product-']"));      // starts with
driver.findElement(By.cssSelector("[id$='-price']"));        // ends with
driver.findElement(By.cssSelector("[id*='product']"));       // contains

// XPath with contains
driver.findElement(By.xpath("//div[contains(@id, 'product-')]"));
driver.findElement(By.xpath("//span[contains(@class, 'price')]"));

// Explicit wait for a dynamically loaded element
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement price = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id*='product'] .price"))
);
```

---

### 23. How do you handle alerts, popups, and frames?

ShopEasy has: a delete confirmation alert, a help chat in an iframe, and an external payment window.

```java
// ALERT — "Are you sure you want to remove this item?"
driver.findElement(By.id("remove-item")).click();
Alert alert = driver.switchTo().alert();
String alertText = alert.getText();  // "Are you sure you want to remove this item?"
alert.accept();    // click OK
// alert.dismiss(); // click Cancel

// IFRAME — Help chat widget embedded in an iframe
driver.switchTo().frame("chat-widget");           // switch into the iframe
driver.findElement(By.id("chat-input")).sendKeys("Help with my order");
driver.findElement(By.id("send-btn")).click();
driver.switchTo().defaultContent();                // switch back to main page

// NEW WINDOW — Payment opens in a new tab
String mainWindow = driver.getWindowHandle();
driver.findElement(By.id("pay-now")).click();       // opens payment in new tab

Set<String> allWindows = driver.getWindowHandles();
for (String window : allWindows) {
    if (!window.equals(mainWindow)) {
        driver.switchTo().window(window);           // switch to payment tab
        break;
    }
}
driver.findElement(By.id("card-number")).sendKeys("4242424242424242");
driver.findElement(By.id("confirm-payment")).click();
driver.close();                                     // close payment tab
driver.switchTo().window(mainWindow);               // back to ShopEasy
```

---

### 24. How do you take a screenshot in Selenium?

A ShopEasy checkout test fails. You need to see what the page looked like at the moment of failure.

```java
// Raw Selenium screenshot
byte[] img = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
Files.write(Path.of("failure.png"), img);

// This framework's approach — attach to Allure report automatically
// In BaseTest.tearDown():
@AfterMethod
public void tearDown(ITestResult result) {
    if (result.getStatus() == ITestResult.FAILURE)
        AllureHelper.screenshot(driver, "Failure");  // appears in the Allure report
    DriverManager.quitDriver();
}

// Manual screenshot during a test for debugging
@Test
public void testCheckout() {
    cartPage.addProduct(42);
    AllureHelper.screenshot(driver, "Cart before checkout");
    cartPage.checkout();
    AllureHelper.screenshot(driver, "After checkout");
}
```

---

### 25. What is headless browser testing?

ShopEasy's CI pipeline runs on a Linux server with no display. You can't open a visible browser.

```java
// Headless — browser runs in memory, no visible window
ChromeOptions opts = new ChromeOptions();
opts.addArguments("--headless");
WebDriver driver = new ChromeDriver(opts);
// Tests execute normally, just nothing visible on screen

// This framework controls it via TestConfig:
// Default: headless=true (for CI)
// Override for local debugging:
// ./gradlew test -Dheadless=false
```

**Headless vs headed:**
| | Headless | Headed |
|---|---|---|
| Speed | ~20% faster | Normal |
| CI/CD | Works without display | Needs Xvfb or display forwarding |
| Debugging | Can't see what's happening | Can watch the test run |
| Screenshots | Still work | Still work |
| When to use | CI pipelines, automated runs | Local development, debugging failures |

---

### 26. How do you handle dropdowns in Selenium?

ShopEasy has a shipping country dropdown and a custom-styled size picker:

```java
// Standard <select> dropdown — use the Select class
Select country = new Select(driver.findElement(By.id("shipping-country")));
country.selectByVisibleText("United States");
country.selectByValue("US");
country.selectByIndex(0);

// Read the selected option
String selected = country.getFirstSelectedOption().getText();
Assert.assertEquals(selected, "United States");

// Get all options
List<WebElement> allCountries = country.getOptions();
Assert.assertTrue(allCountries.size() > 100);


// Custom dropdown (styled div, NOT a <select>) — click to open, click option
driver.findElement(By.cssSelector(".size-picker .toggle")).click();  // open dropdown
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".size-picker .options")));
driver.findElement(By.cssSelector(".size-picker .option[data-size='M']")).click();
```

---

### 27. What is the difference between close() and quit()?

ShopEasy's payment opens in a new tab:

```java
// close() — closes only the CURRENT tab/window
driver.findElement(By.id("pay-now")).click();  // opens new tab
driver.switchTo().window(paymentTab);
// ... complete payment ...
driver.close();  // closes payment tab ONLY, main tab still open
driver.switchTo().window(mainTab);  // back to ShopEasy

// quit() — closes ALL tabs AND ends the WebDriver process
driver.quit();  // everything closed, browser process terminated

// In this framework, DriverManager always uses quit():
public static void quitDriver() {
    if (driver.get() != null) {
        driver.get().quit();    // close everything
        driver.remove();         // clean up ThreadLocal
    }
}
```

---

### 28. How do you execute JavaScript in Selenium?

ShopEasy has some elements that Selenium can't interact with normally:

```java
JavascriptExecutor js = (JavascriptExecutor) driver;

// Scroll to the "Load More Products" button at the bottom of the page
WebElement loadMore = driver.findElement(By.id("load-more"));
js.executeScript("arguments[0].scrollIntoView(true);", loadMore);

// Click a button hidden behind a cookie banner overlay
WebElement checkout = driver.findElement(By.id("checkout"));
js.executeScript("arguments[0].click();", checkout);  // bypasses the overlay

// Get the total price from a hidden data attribute
String rawPrice = (String) js.executeScript(
    "return document.getElementById('cart-total').getAttribute('data-raw-price');"
);

// Wait for React to finish rendering
js.executeScript("return document.readyState").equals("complete");

// Set a value on a React-controlled input (sendKeys doesn't always work with React)
js.executeScript(
    "var input = arguments[0]; " +
    "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set; " +
    "nativeInputValueSetter.call(input, arguments[1]); " +
    "input.dispatchEvent(new Event('input', { bubbles: true }));",
    element, "new value"
);
```

---

### 29. How do you handle file uploads in Selenium?

ShopEasy sellers upload product images:

```java
// Standard <input type="file"> — sendKeys with file path
WebElement upload = driver.findElement(By.id("product-image"));
upload.sendKeys("/Users/tester/images/headphones.jpg");

// Wait for the upload preview to appear
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("image-preview")));

// Verify the file name is shown
String fileName = driver.findElement(By.cssSelector(".upload-name")).getText();
Assert.assertEquals(fileName, "headphones.jpg");
```

---

### 30. What are ChromeOptions flags and when do you use them?

Different environments need different Chrome settings:

```java
ChromeOptions opts = new ChromeOptions();

// CI/Docker — no display, limited resources
opts.addArguments("--headless");               // no visible window
opts.addArguments("--no-sandbox");             // required inside Docker containers
opts.addArguments("--disable-dev-shm-usage");  // /dev/shm is too small in Docker
opts.addArguments("--disable-gpu");            // GPU not available in CI

// Consistent screenshots — fixed window size regardless of host machine
opts.addArguments("--window-size=1920,1080");

// Testing — isolate from local browser state
opts.addArguments("--incognito");              // no cookies, no cache from your real browsing

// Mobile emulation — test responsive design
Map<String, String> mobileEmulation = Map.of("deviceName", "iPhone 12 Pro");
opts.setExperimentalOption("mobileEmulation", mobileEmulation);

// Performance — disable images and CSS to speed up loading
Map<String, Object> prefs = Map.of("profile.managed_default_content_settings.images", 2);
opts.setExperimentalOption("prefs", prefs);

WebDriver driver = new ChromeDriver(opts);
```

---

## Part 3: Java (Questions 31–40)

*The Java concepts powering the ShopEasy framework.*

---

### 31. What is the difference between abstract class and interface?

ShopEasy has different page types. Should `BasePage` be an abstract class or interface?

```java
// ABSTRACT CLASS — chosen because we need state (driver, wait) and shared behavior
public abstract class BasePage {
    protected WebDriver driver;             // instance variable — interfaces can't have these
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {     // constructor — interfaces can't have these
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected void click(By locator) {      // concrete method — shared by all pages
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected abstract String getPageUrl(); // abstract — each page must define its own URL
}

// INTERFACE — use when you only need a contract with no shared state
public interface Searchable {
    void search(String query);
    List<String> getResults();
}

// A page can extend ONE abstract class but implement MANY interfaces
public class ProductPage extends BasePage implements Searchable {
    public String getPageUrl() { return "/products"; }
    public void search(String query) { type(searchBox, query); }
    public List<String> getResults() { /* ... */ }
}
```

| Decision | Use Abstract Class | Use Interface |
|----------|-------------------|---------------|
| Need instance variables? | Yes → abstract class | No |
| Need a constructor? | Yes → abstract class | No |
| Need shared method implementations? | Yes → abstract class | Use default methods (Java 8+) |
| Multiple inheritance needed? | No | Yes → interface |

---

### 32. What is CompletableFuture and how is it used?

Loading ShopEasy's homepage requires 4 independent API calls. Without async:

```java
// Sequential — 2 seconds total
Response featured = api.get("/products/featured");  // 500ms
Response deals    = api.get("/deals");               // 400ms
Response reviews  = api.get("/reviews/top");          // 600ms
Response banners  = api.get("/banners");              // 500ms
// Total: 500 + 400 + 600 + 500 = 2000ms

// With CompletableFuture — 600ms total (max of all four)
CompletableFuture<Response> featured = CompletableFuture.supplyAsync(() -> api.get("/products/featured"), executor);
CompletableFuture<Response> deals    = CompletableFuture.supplyAsync(() -> api.get("/deals"), executor);
CompletableFuture<Response> reviews  = CompletableFuture.supplyAsync(() -> api.get("/reviews/top"), executor);
CompletableFuture<Response> banners  = CompletableFuture.supplyAsync(() -> api.get("/banners"), executor);

// Wait for ALL four to complete
CompletableFuture.allOf(featured, deals, reviews, banners).join();

// Now get results
Assert.assertEquals(featured.join().statusCode(), 200);
Assert.assertTrue(deals.join().jsonPath().getList("$").size() > 0);
```

Chaining async operations:
```java
// Login → then fetch profile → then fetch orders (each depends on the previous)
CompletableFuture<String> orderStatus = CompletableFuture
    .supplyAsync(() -> api.post("/auth/login", credentials))    // login
    .thenApply(r -> r.jsonPath().getString("token"))             // extract token
    .thenApply(token -> { api.setJwtToken(token); return token; }) // set token
    .thenApply(token -> api.get("/orders/latest"))               // fetch latest order
    .thenApply(r -> r.jsonPath().getString("status"));           // extract status

String status = orderStatus.join();  // "shipped"
```

---

### 33. What is ThreadLocal?

Four testers run ShopEasy UI tests in parallel. Each needs their own browser:

```java
// WITHOUT ThreadLocal — disaster
public class DriverManager {
    private static WebDriver driver;  // shared across all threads

    // Thread 1 calls getDriver() → Chrome opens → navigates to /login
    // Thread 2 calls getDriver() → SAME Chrome! → navigates to /products (Thread 1 lost /login!)
    // Thread 1 tries to find login form → CRASH: it's on the products page
}

// WITH ThreadLocal — each thread is isolated
public class DriverManager {
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            driver.set(new ChromeDriver());  // new browser for THIS thread only
        }
        return driver.get();  // always returns THIS thread's browser
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();  // closes THIS thread's browser only
            driver.remove();      // prevents memory leak
        }
    }
}

// Thread 1: DriverManager.getDriver() → Chrome A → /login
// Thread 2: DriverManager.getDriver() → Chrome B → /products
// Thread 3: DriverManager.getDriver() → Chrome C → /cart
// Thread 4: DriverManager.getDriver() → Chrome D → /checkout
// All running simultaneously, no interference
```

---

### 34. What is the difference between == and .equals()?

Comparing strings in ShopEasy tests:

```java
String expected = "Order Confirmed";
String actual = driver.findElement(By.id("status")).getText();

// == checks REFERENCES — are they the exact same object in memory?
actual == expected         // UNPREDICTABLE — might be true or false
actual == "Order Confirmed"  // UNPREDICTABLE — depends on string interning

// .equals() checks VALUES — do they contain the same characters?
actual.equals(expected)    // RELIABLE — true if text matches
actual.equals("Order Confirmed")  // RELIABLE

// Common gotcha with integers
Integer a = 200;
Integer b = 200;
a == b          // FALSE! (integers > 127 are not cached)
a.equals(b)     // true

// ShopEasy example
Response r = api.get("/products/42");
// BAD
Assert.assertTrue(r.jsonPath().getString("name") == "Laptop");  // might fail!
// GOOD
Assert.assertEquals(r.jsonPath().getString("name"), "Laptop");  // uses .equals()
```

---

### 35. What are generics and why use them?

Building a type-safe response wrapper for ShopEasy:

```java
// WITHOUT generics — dangerous, anything goes
List products = api.getProducts();
String name = (String) products.get(0);  // ClassCastException if it's not a String!

// WITH generics — compiler catches errors
List<Product> products = api.getProducts();
Product first = products.get(0);  // guaranteed to be a Product
String name = first.getName();    // safe

// ShopEasy generic API response wrapper
public class ApiResponse<T> {
    private int statusCode;
    private T data;
    private String error;

    public T getData() { return data; }
}

// Usage — type-safe for any resource
ApiResponse<Product> productResponse = api.getProduct(42);
Product product = productResponse.getData();  // no casting needed

ApiResponse<List<Order>> ordersResponse = api.getOrders();
List<Order> orders = ordersResponse.getData();  // type-safe list
```

---

### 36. What is a Map and how is Map.of() used?

Building JSON request bodies for ShopEasy API calls:

```java
// Map.of() — create an immutable map inline
// Perfect for API request bodies
Map<String, Object> newProduct = Map.of(
    "name", "Wireless Headphones",
    "price", 79.99,
    "category", "Electronics"
);
Response r = api.post("/products", newProduct);
// REST Assured serializes this to: {"name":"Wireless Headphones","price":79.99,"category":"Electronics"}

// Nested maps for complex structures
Map<String, Object> order = Map.of(
    "productId", 42,
    "quantity", 2,
    "shipping", Map.of(
        "address", "123 Main St",
        "city", "Austin",
        "state", "TX"
    )
);

// Limitation: Map.of() only supports up to 10 key-value pairs
// For larger maps, use Map.ofEntries():
Map<String, Object> bigMap = Map.ofEntries(
    Map.entry("field1", "value1"),
    Map.entry("field2", "value2"),
    // ... up to any number of entries
    Map.entry("field15", "value15")
);

// Map.of() is immutable — you can't add or change entries after creation
Map<String, Object> data = Map.of("name", "Test");
data.put("price", 9.99);  // UnsupportedOperationException!

// If you need mutable, use HashMap:
Map<String, Object> data = new HashMap<>();
data.put("name", "Test");
data.put("price", 9.99);  // works
```

---

### 37. What is the difference between checked and unchecked exceptions?

Exceptions you encounter while testing ShopEasy:

```java
// UNCHECKED (RuntimeException) — don't need to be caught
// These are bugs in your code — fix the code, not the exception

driver.findElement(By.id("nonexistent"));
// → NoSuchElementException (element not on page)

String s = null;
s.length();
// → NullPointerException (forgot to initialize)

List<String> list = List.of("a", "b");
list.get(5);
// → IndexOutOfBoundsException (list only has 2 items)


// CHECKED — compiler FORCES you to handle them
// These are expected failures — I/O, network, files

// Loading test data from a file
try {
    JsonObject data = DataHelper.loadTestData("testData.json");
} catch (FileNotFoundException e) {
    System.out.println("Test data file missing: " + e.getMessage());
}

// Waiting between retries
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

// Reading a config file
try (FileReader reader = new FileReader("config.properties")) {
    Properties props = new Properties();
    props.load(reader);
} catch (IOException e) {
    throw new RuntimeException("Cannot load config", e);
}
```

---

### 38. What is the static keyword?

ShopEasy framework classes use static for shared utilities:

```java
// STATIC FIELD — one shared value across every instance of the class
public class TestConfig {
    public static final String BASE_URL = "https://shopeasy.com";
    // Every test, every page object uses THE SAME URL
    // TestConfig.BASE_URL — accessed on the class, no instance needed
}

// STATIC METHOD — utility functions that don't need object state
public class DataHelper {
    public static String randomEmail() {
        return "test_" + randomString(6) + "@test.com";
    }
    // Called as: DataHelper.randomEmail()
    // No need to create a DataHelper object
}

// NON-STATIC — each instance has its own copy
public class LoginPage extends BasePage {
    private final By inputEmail = By.id("email");  // each LoginPage has its own locators

    public void login(String email, String password) {
        // 'this' refers to THIS specific LoginPage instance
        type(this.inputEmail, email);
    }
}

/// Why DriverManager uses "static final ThreadLocal":
// Let's break it down word by word.

// PROBLEM: We need DriverManager.getDriver() to work from anywhere (static),
// but each parallel thread needs its OWN separate browser (not static).
// These two goals contradict each other. ThreadLocal solves this.

public class DriverManager {

    // Think of ThreadLocal as a locker room with named lockers.
    // There is ONE locker room (static) — shared across the whole framework.
    // But inside, each thread gets its OWN locker with its OWN WebDriver.

    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    //       ------       -----------
    //       static       ThreadLocal
    //       "one         "but each thread
    //       locker       has its own
    //       room"        locker inside"

    // Without static: you'd need a DriverManager object to call getDriver().
    //   DriverManager dm = new DriverManager();  // annoying, who holds this?
    //   dm.getDriver();

    // With static: call it anywhere, no object needed.
    //   DriverManager.getDriver();  // clean, works from any class

    // Without ThreadLocal: one WebDriver shared by all threads = crashes.
    //   private static WebDriver driver;
    //   Thread 1 sets driver to Chrome A → navigates to /login
    //   Thread 2 sets driver to Chrome B → OVERWRITES Chrome A!
    //   Thread 1 calls getDriver() → gets Chrome B → wrong page → crash

    // With ThreadLocal: each thread stores and retrieves its OWN value.
    //   Thread 1: driver.set(Chrome A) → driver.get() returns Chrome A
    //   Thread 2: driver.set(Chrome B) → driver.get() returns Chrome B
    //   Thread 1: driver.get() → still Chrome A (unaffected by Thread 2)

    public static WebDriver getDriver() { return driver.get(); }
    // static → callable as DriverManager.getDriver()
    // driver.get() → returns THIS thread's WebDriver, not anyone else's
}

// ANALOGY:
// static WebDriver           = one shared parking spot, everyone fights over it
// static ThreadLocal<>       = one parking GARAGE, but each thread has a reserved spot
//
//   Garage (static — one garage exists)
//   ┌──────────────────────────────────┐
//   │  Spot 1: Thread-1's Chrome A     │  ← driver.get() from Thread 1
//   │  Spot 2: Thread-2's Chrome B     │  ← driver.get() from Thread 2
//   │  Spot 3: Thread-3's Firefox C    │  ← driver.get() from Thread 3
//   └──────────────────────────────────┘
//   Each thread only sees its own spot.
```

---

### 39. What is method overriding vs overloading?

Making the ShopEasy page objects flexible:

```java
// OVERLOADING — same method name, DIFFERENT parameters (same class)
public class ApiClient {
    // Three ways to call GET — all named "get" but with different signatures
    public Response get(String endpoint) {
        return req().get(endpoint).then().extract().response();
    }

    public Response get(String endpoint, Map<String, String> queryParams) {
        return req().queryParams(queryParams).get(endpoint).then().extract().response();
    }

    public Response get(String endpoint, Map<String, String> queryParams, Map<String, String> headers) {
        return req().queryParams(queryParams).headers(headers).get(endpoint).then().extract().response();
    }
}

// Usage — compiler picks the right method based on arguments
api.get("/products");
api.get("/products", Map.of("category", "Electronics"));
api.get("/products", Map.of("category", "Electronics"), Map.of("X-Custom", "value"));


// OVERRIDING — subclass REPLACES a parent method (same signature)
public abstract class BasePage {
    protected void open(String path) {
        driver.get(TestConfig.BASE_URL + path);
    }
}

public class LoginPage extends BasePage {
    @Override
    protected void open(String path) {
        super.open(path);
        // Additional step: wait for the login form to load
        waitForVisible(By.id("login-form"));
    }

    // Also has an OVERLOADED version (no parameters)
    public void open() {
        open("/login");  // calls the overridden version with a default path
    }
}
```

---

### 40. What is the final keyword?

Preventing unwanted changes in the ShopEasy framework:

```java
// FINAL VARIABLE — once set, cannot be changed
public class LoginPage {
    private final By inputEmail = By.id("email");  // locator can never be reassigned
    // inputEmail = By.name("email");  // COMPILE ERROR: cannot reassign final

    public LoginPage(WebDriver driver) {
        // driver is a parameter, but if it were final:
        // final WebDriver driver → you couldn't reassign it inside this method
    }
}

// FINAL METHOD — subclasses cannot override it
public class ApiClient {
    @SafeVarargs
    public final void awaitAll(CompletableFuture<Response>... futures) {
        CompletableFuture.allOf(futures).join();
    }
    // No subclass can change how awaitAll works — it's locked down
}

// FINAL CLASS — cannot be extended at all
public final class TestConfig {
    public static final String BASE_URL = "https://shopeasy.com";
    // No one can write: class MyConfig extends TestConfig — prevents accidents
}

// Why use final?
// 1. Locators (By) — should never change after initialization
// 2. Constants — BASE_URL, TIMEOUT should be set once
// 3. Thread safety — final fields are safely published across threads
// 4. Clarity — tells other developers "don't change this"
```

---

## Part 4: TestNG (Questions 41–45)

*Organizing and running ShopEasy tests.*

---

### 41. What are the key TestNG annotations and their execution order?

ShopEasy test lifecycle for a checkout flow:

```
@BeforeSuite         → Start test report server, connect to test database
  @BeforeTest        → Set environment (staging vs production)
    @BeforeClass     → Create API client, authenticate
      @BeforeMethod  → Open fresh browser, navigate to starting page
        @Test        → testAddToCart(), testCheckout(), testPayment()
      @AfterMethod   → Screenshot on failure, close browser
    @AfterClass      → Shutdown API client thread pool
  @AfterTest         → Clear test data from database
@AfterSuite          → Generate final report, send Slack notification
```

In this framework:
```java
// API tests — expensive setup, done once per class
public class UserApiTest {
    @BeforeClass
    public void setUp() { api = new UserApi(); }   // one client for all tests
    @AfterClass
    public void tearDown() { api.shutdown(); }      // cleanup once
}

// UI tests — fresh browser for EVERY test (isolation)
public abstract class BaseTest {
    @BeforeMethod
    public void setUp() { driver = DriverManager.getDriver(); }   // new browser each time
    @AfterMethod
    public void tearDown() { DriverManager.quitDriver(); }         // close after each test
}
```

---

### 42. What is the difference between @BeforeClass and @BeforeMethod?

```java
public class ShopEasyCheckoutTest extends BaseTest {

    // @BeforeClass — runs ONCE before all tests in this class
    // Use for expensive setup that all tests share
    @BeforeClass
    public void classSetup() {
        api = new UserApi();                    // create API client (expensive)
        api.authenticate("admin", "pass");      // login once
        testProductId = createTestProduct();     // create test data once
    }

    // @BeforeMethod — runs before EACH test method
    // Use for per-test isolation
    @BeforeMethod
    public void methodSetup() {
        driver = DriverManager.getDriver();     // fresh browser
        driver.manage().window().maximize();
        loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login("customer@shopeasy.com", "pass123");
    }

    @Test public void testAddToCart() { /* starts with logged-in browser */ }
    @Test public void testCheckout()  { /* starts with logged-in browser */ }
    @Test public void testPayment()   { /* starts with logged-in browser */ }

    // Execution timeline:
    // classSetup()        ← once
    //   methodSetup()     ← before testAddToCart
    //   testAddToCart()
    //   tearDown()
    //   methodSetup()     ← before testCheckout
    //   testCheckout()
    //   tearDown()
    //   methodSetup()     ← before testPayment
    //   testPayment()
    //   tearDown()
    // classTearDown()     ← once
}
```

---

### 43. What is a DataProvider?

Testing ShopEasy login with multiple credential combinations:

```java
@DataProvider(name = "loginScenarios")
public Object[][] loginData() {
    return new Object[][] {
        // email,                      password,              expectedResult
        {"customer@shopeasy.com",      "correctPass",         "Dashboard"},
        {"admin@shopeasy.com",         "adminPass",           "Admin Panel"},
        {"invalid@shopeasy.com",       "wrongPass",           "Invalid credentials"},
        {"",                           "somePass",            "Email is required"},
        {"customer@shopeasy.com",      "",                    "Password is required"},
        {"notanemail",                 "pass123",             "Invalid email format"},
    };
}

@Test(dataProvider = "loginScenarios")
public void testLogin(String email, String password, String expectedResult) {
    loginPage.open();
    loginPage.login(email, password);

    if (expectedResult.equals("Dashboard") || expectedResult.equals("Admin Panel")) {
        Assert.assertTrue(dashboardPage.getHeaderText().contains(expectedResult));
    } else {
        Assert.assertTrue(loginPage.getFlashMessage().contains(expectedResult));
    }
}
// This runs 6 times — once per row — in the test report, each shows as a separate test

// DataProvider from a JSON file:
@DataProvider(name = "productsFromFile")
public Object[][] productData() {
    JsonObject data = DataHelper.loadTestData("testData.json");
    JsonArray products = data.getAsJsonArray("products");
    Object[][] result = new Object[products.size()][3];
    for (int i = 0; i < products.size(); i++) {
        JsonObject p = products.get(i).getAsJsonObject();
        result[i] = new Object[]{
            p.get("name").getAsString(),
            p.get("price").getAsDouble(),
            p.get("category").getAsString()
        };
    }
    return result;
}
```

---

### 44. How does TestNG handle test dependencies?

ShopEasy's checkout flow must run in order:

```java
@Test(priority = 1)
public void testLogin() {
    loginPage.login("customer@shopeasy.com", "pass123");
    Assert.assertTrue(loginPage.isLoggedIn());
}

@Test(priority = 2, dependsOnMethods = "testLogin")
public void testAddToCart() {
    productPage.addToCart(42);
    Assert.assertEquals(cartPage.getItemCount(), 1);
}

@Test(priority = 3, dependsOnMethods = "testAddToCart")
public void testCheckout() {
    cartPage.checkout();
    Assert.assertTrue(checkoutPage.isLoaded());
}

@Test(priority = 4, dependsOnMethods = "testCheckout")
public void testPayment() {
    checkoutPage.pay("4242424242424242", "12/27", "123");
    Assert.assertTrue(confirmationPage.getOrderNumber().startsWith("ORD-"));
}

// If testLogin fails:
//   testAddToCart  → SKIPPED (not failed — clearly shows the root cause)
//   testCheckout   → SKIPPED
//   testPayment    → SKIPPED
// Report shows: 1 failed, 3 skipped — you know Login is the problem
```

---

### 45. What is soft assertion vs hard assertion?

Validating a ShopEasy product page with many fields:

```java
// HARD ASSERTION — stops at first failure
@Test
public void testProductDetailsHard() {
    Response r = productApi.getProduct(42);
    Assert.assertEquals(r.statusCode(), 200);
    Assert.assertEquals(r.jsonPath().getString("name"), "Laptop");     // FAILS here
    Assert.assertEquals(r.jsonPath().getDouble("price"), 999.99);     // NEVER CHECKED
    Assert.assertEquals(r.jsonPath().getString("category"), "Electronics"); // NEVER CHECKED
    // You only know about the name being wrong. Run again to find more issues?
}

// SOFT ASSERTION — collects ALL failures, reports at the end
@Test
public void testProductDetailsSoft() {
    SoftAssert soft = new SoftAssert();
    Response r = productApi.getProduct(42);

    soft.assertEquals(r.statusCode(), 200);
    soft.assertEquals(r.jsonPath().getString("name"), "Laptop");           // ✗ FAILS
    soft.assertEquals(r.jsonPath().getDouble("price"), 999.99);           // ✗ FAILS
    soft.assertEquals(r.jsonPath().getString("category"), "Electronics"); // ✓ PASSES
    soft.assertTrue(r.jsonPath().getList("tags").size() > 0);             // ✓ PASSES

    soft.assertAll();  // NOW reports: 2 failures out of 5 assertions
    // You immediately see: name AND price are wrong, category and tags are fine
}
```

**When to use each:**
| Scenario | Use |
|----------|-----|
| Status code check (if wrong, nothing else matters) | Hard |
| Validating multiple fields on a response | Soft |
| Sequential flow (login → navigate → click) | Hard |
| Comparing full API response to expected | Soft |

---

## Part 5: Gradle & Build (Questions 46–50)

*Building, running, and configuring the ShopEasy test suite.*

---

### 46. What is Gradle and how does it differ from Maven?

ShopEasy uses Gradle. Here's the build.gradle:

```groovy
plugins {
    id 'java'
    id 'io.qameta.allure' version '2.11.2'
}

repositories {
    mavenCentral()
}

dependencies {
    // Production code (src/main) — frameworks the page objects and API client depend on
    implementation 'org.seleniumhq.selenium:selenium-java:4.15.0'
    implementation 'io.rest-assured:rest-assured:5.3.0'
    implementation 'io.qameta.allure:allure-testng:2.24.0'
    implementation 'io.github.bonigarcia:webdrivermanager:5.6.2'
    implementation 'com.google.code.gson:gson:2.10.1'

    // Test code only (src/test)
    testImplementation 'org.testng:testng:7.8.0'
}

test {
    useTestNG()
    systemProperties = System.getProperties()  // forward -D flags to tests
}
```

Equivalent in Maven (`pom.xml`):
```xml
<dependencies>
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>4.15.0</version>
    </dependency>
    <dependency>
        <groupId>org.testng</groupId>
        <artifactId>testng</artifactId>
        <version>7.8.0</version>
        <scope>test</scope>
    </dependency>
    <!-- ... 20 more lines of XML per dependency ... -->
</dependencies>
```

| Feature | Gradle | Maven |
|---------|--------|-------|
| Syntax | Groovy/Kotlin (concise) | XML (verbose) |
| Speed | Incremental builds, build cache | Full rebuild each time |
| Adding a dependency | 1 line | 5-8 lines of XML |
| Customization | Groovy scripting — any logic | Plugins only |
| Learning curve | Steeper | Simpler conventions |

---

### 47. What is the Gradle wrapper and why use it?

A new developer clones the ShopEasy repo. They don't have Gradle installed:

```bash
# Without wrapper — "command not found: gradle"
gradle test  # fails if Gradle isn't installed or is wrong version

# With wrapper — always works, downloads the right version automatically
./gradlew test  # downloads Gradle 8.4 (pinned in gradle-wrapper.properties), then runs

# On Windows
gradlew.bat test
```

The wrapper files:
```
gradlew                           # shell script (macOS/Linux)
gradlew.bat                       # batch script (Windows)
gradle/wrapper/
    gradle-wrapper.jar            # tiny bootloader
    gradle-wrapper.properties     # specifies exact Gradle version
```

`gradle-wrapper.properties`:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
```

Benefits:
- CI server has no Gradle? No problem — wrapper downloads it
- Team member on Gradle 7, you're on 8? No problem — wrapper enforces 8.4
- No "works on my machine" build issues

---

### 48. How do you pass system properties to tests via Gradle?

Running ShopEasy tests against different environments:

```bash
# Local development — headed browser, staging API
./gradlew test -DbaseUrl=https://staging.shopeasy.com \
               -DapiBaseUrl=https://api-staging.shopeasy.com \
               -Dbrowser=chrome \
               -Dheadless=false \
               -Dtimeout=15

# CI pipeline — headless, production-like environment
./gradlew test -DbaseUrl=https://qa.shopeasy.com \
               -DapiBaseUrl=https://api-qa.shopeasy.com \
               -Dbrowser=chrome \
               -Dheadless=true \
               -Dtimeout=10

# Quick smoke test with Firefox
./gradlew test --tests "ui.LoginTest" -Dbrowser=firefox -Dheadless=false
```

In build.gradle, forward these to the test JVM:
```groovy
test {
    useTestNG()
    systemProperties = System.getProperties()
    // This line makes -Dbrowser=firefox available as System.getProperty("browser") in Java
}
```

In `TestConfig.java`, they're read with defaults:
```java
public static final String BROWSER = System.getProperty("browser", "chrome");
// If -Dbrowser=firefox was passed → "firefox"
// If nothing passed → "chrome" (default)
```

---

### 49. How do you run specific tests with Gradle?

```bash
# Run ALL tests
./gradlew test

# Run all UI tests (everything in the ui package)
./gradlew test --tests "ui.*"

# Run all API tests
./gradlew test --tests "api.*"

# Run a specific test class
./gradlew test --tests "ui.LoginTest"

# Run a specific test method
./gradlew test --tests "ui.LoginTest.testValidLogin"

# Run by pattern — anything with "checkout" in the name
./gradlew test --tests "*Checkout*"

# Run multiple specific classes
./gradlew test --tests "ui.LoginTest" --tests "ui.DashboardTest"

# Force re-run even if nothing changed (Gradle caches results)
./gradlew test --rerun

# Run with verbose output
./gradlew test --info
```

---

### 50. What is the difference between implementation and testImplementation?

```groovy
dependencies {
    // IMPLEMENTATION — available to src/main/java AND src/test/java
    // These are used by the framework itself (page objects, API client)
    implementation 'org.seleniumhq.selenium:selenium-java:4.15.0'     // BasePage uses WebDriver
    implementation 'io.rest-assured:rest-assured:5.3.0'               // ApiClient uses RestAssured
    implementation 'com.google.code.gson:gson:2.10.1'                  // DataHelper uses Gson
    implementation 'io.qameta.allure:allure-testng:2.24.0'            // AllureHelper uses Allure
    implementation 'io.github.bonigarcia:webdrivermanager:5.6.2'      // DriverManager uses WebDriverManager

    // TEST IMPLEMENTATION — available ONLY to src/test/java
    // These are used only by the test classes themselves
    testImplementation 'org.testng:testng:7.8.0'                      // @Test, Assert, @BeforeMethod
}
```

Why does it matter?
- If ShopEasy's framework were published as a library, `testImplementation` dependencies would NOT be included
- Keeps the production artifact lightweight
- Makes it clear which dependencies are for testing vs runtime

```
src/main/java/                          src/test/java/
├── api/ApiClient.java     ← uses       ├── api/UserApiTest.java    ← uses
│   rest-assured ✓                      │   testng ✓
│   gson ✓                              │   rest-assured ✓ (inherited)
│   allure ✓                            │   gson ✓ (inherited)
├── pages/BasePage.java    ← uses       ├── ui/LoginTest.java       ← uses
│   selenium ✓                          │   testng ✓
│   webdrivermanager ✓                  │   selenium ✓ (inherited)
```
