# WebDriver Java Gradle Framework

A test automation framework for **UI** and **API** testing built with Selenium WebDriver, REST Assured, TestNG, and Allure reporting.

## Project Structure

src/
├── main/java/
│ ├── api/
│ │ ├── ApiClient.java # Base HTTP client (REST Assured, JWT, async)
│ │ └── UserApi.java # User API endpoint definitions
│ ├── config/
│ │ └── TestConfig.java # Centralized configuration (URLs, browser, timeouts)
│ ├── helpers/
│ │ ├── AllureHelper.java # Screenshot/attachment utilities for Allure reports
│ │ ├── DataHelper.java # JSON test data loader and random data generators
│ │ └── DriverManager.java # Thread-safe WebDriver lifecycle manager
│ └── pages/
│ ├── BasePage.java # Abstract page with waits and common actions
│ ├── DashboardPage.java # Secure Area / Dashboard page object
│ └── LoginPage.java # Login page object
│
└── test/
├── java/
│ ├── api/
│ │ └── UserApiTest.java # API tests (sync, async, JWT)
│ └── ui/
│ ├── BaseTest.java # Abstract test with browser setup/teardown
│ ├── DashboardTest.java # Dashboard page tests
│ ├── LoginTest.java # Login flow tests (valid, invalid, empty fields)
│ └── WaitDemoTest.java # Demonstrates implicit, explicit, and fluent waits
└── resources/
└── testData.json # Test credentials and data

## Architecture
┌─────────────────────────────────────────────────────────────────┐
│ TEST LAYER │
│ LoginTest / DashboardTest / UserApiTest / WaitDemoTest │
│ (assertions only — no HTTP or browser details) │
├─────────────────────────────────────────────────────────────────┤
│ SERVICE LAYER │
│ UI: LoginPage, DashboardPage (extend BasePage) │
│ API: UserApi (extends ApiClient) │
├─────────────────────────────────────────────────────────────────┤
│ INFRASTRUCTURE LAYER │
│ BasePage (waits, actions) │ ApiClient (REST Assured, JWT) │
│ DriverManager (WebDriver) │ TestConfig (configuration) │
│ AllureHelper (reporting) │ DataHelper (test data) │
└─────────────────────────────────────────────────────────────────┘


## Prerequisites

- **Java 11+**
- **Gradle** (wrapper included)
- **Chrome** and/or **Firefox** browser installed

## Running Tests

### Run all tests
```bash
./gradlew test
```
### Run API tests only
```bash
./gradlew test --tests "api.*"
```
### Run UI tests only
```bash
./gradlew test --tests "ui.*"
```
### Generate Allure report
```bash
./gradlew allureReport
```
Then open `build/reports/allure-report/index.html` in a browser.
### Run with custom config (e.g., headless mode)
```bash
HEADLESS=true ./gradlew test
```
### Run a specific test method
```bash
./gradlew test --tests "ui.LoginTest.testValidLogin"
```
### Clean and run
```bash
./gradlew clean test
```
### Parallel execution
```bash
./gradlew test --parallel
```
### Debug mode
```bash
./gradlew test --debug-jvm
```
Then attach a debugger to the specified port (default 5005).
### Continuous testing (rerun on file changes)
```bash
./gradlew test --continuous
```
This will rerun tests whenever source files change, ideal for TDD.
### Test filtering by tags (e.g., @SmokeTest)
```bash
./gradlew test --tests "*SmokeTest*"
```
This requires adding TestNG groups to your test methods and configuring the test task to include/exclude
groups as needed.

## Configuration
All settings are controlled via JVM system properties with sensible defaults:

Property	Default	Description
baseUrl	https://the-internet.herokuapp.com	Base URL for UI tests
apiBaseUrl	https://jsonplaceholder.typicode.com	Base URL for API tests
browser	chrome	Browser: chrome or firefox
headless	true	Run browser without UI
timeout	10	Wait timeout in seconds

Override at runtime
```bash
BASE_URL=http://localhost:8080/api BROWSER=firefox HEADLESS=false ./gradlew test
```


### Custom test listeners
You can implement TestNG listeners (e.g., for logging, retry logic) and register them
in `src/test/resources/testng.xml` or via annotations in your test classes.

### Environment variables
- `BASE_URL` — Override the base URL for API and UI tests
- `BROWSER` — Specify browser for UI tests (e.g., `chrome`, `
firefox`)
- `HEADLESS` — Set to `true` to run browsers in headless mode
- `API_TIMEOUT` — Set custom timeout for API requests (in seconds)
- `ALLURE_RESULTS_DIR` — Specify custom directory for Allure results
- `ALLURE_REPORT_DIR` — Specify custom directory for Allure reports
- `TEST_DATA_FILE` — Specify a custom JSON file for test data
- `LOG_LEVEL` — Set logging level (e.g., `DEBUG`, `INFO`,
`ERROR`)
- `RETRY_COUNT` — Set the number of retries for failed tests (requires retry logic
implementation)
- `PARALLEL_THREADS` — Set the number of threads for parallel test execution
- `DEBUG_JVM` — Set to `true` to enable JVM debug mode on port
5005
- `TESTNG_GROUPS` — Comma-separated list of TestNG groups to include/ex
clude in test runs
- `TESTNG_LISTENERS` — Comma-separated list of fully qualified TestNG listener classes to register
- `ALLURE_ENABLE_SCREENCAPTURE` — Set to `true` to enable
automatic screenshot capture on test failure for Allure reports
- `ALLURE_ENABLE_LOGCAPTURE` — Set to `true` to enable automatic log
capture on test failure for Allure reports
- `ALLURE_ENABLE_ENVIRONMENT` — Set to `true` to include environment
variables in Allure reports
- `ALLURE_ENABLE_CATEGORIES` — Set to `true` to include test categories (
e.g., severity) in Allure reports
- `ALLURE_ENABLE_HISTORY` — Set to `true` to enable test history tracking in
Allure reports
- `ALLURE_ENABLE_TIMELINE` — Set to `true` to enable timeline view
in Allure reports
- `ALLURE_ENABLE_EXECUTORS` — Set to `true` to include executor information
in Allure reports
- `ALLURE_ENABLE_LABELS` — Set to `true` to include custom labels in
Allure reports
- `ALLURE_ENABLE_LINKS` — Set to `true` to include links (e
.g., issue tracker, documentation) in Allure reports
- `ALLURE_ENABLE_PARAMETERS` — Set to `true` to include test parameters in
Allure reports
- `ALLURE_ENABLE_ATTACHMENTS` — Set to `true` to enable attachments (e
.g., screenshots, logs) in Allure reports
- `ALLURE_ENABLE_STEPS` — Set to `true` to enable step logging in
Allure reports
- `ALLURE_ENABLE_SEVERITY` — Set to `true` to include severity levels
in Allure reports
- `ALLURE_ENABLE_EPICS` — Set to `true` to include epics in
Allure reports
- `ALLURE_ENABLE_FEATURES` — Set to `true` to include features in
Allure reports


## Test Data Management
Test data is stored in `src/test/resources/testData.json` and loaded via `DataHelper`. You can specify a different file using the `TEST_DATA_FILE` environment variable. The JSON structure can include user credentials, API payload templates, and any other data needed for tests.

## Key Design Patterns

## Page Object Model (UI)
Each web page is represented by a class that extends BasePage. Tests interact with page methods (login(), logout()) instead of raw Selenium calls. Locators and browser interactions are encapsulated inside page objects.

## Service Object Pattern (API)
Each API domain has a service class (e.g., UserApi) that extends ApiClient. Tests call domain methods (getUsers(), createUser()) instead of raw HTTP calls. REST Assured is used internally but never exposed to tests.

## Thread-Safe WebDriver
DriverManager uses ThreadLocal to isolate browser instances per thread, enabling safe parallel test execution.

## Wait Strategies
The framework supports three Selenium wait strategies (demonstrated in WaitDemoTest):

Strategy	Scope	When to Use
Implicit Wait	Global	Avoid — conflicts with explicit waits
Explicit Wait	Per-element	Default choice — condition-based, precise
Fluent Wait	Per-element	Unpredictable load times, custom polling needed

## API Testing
The API layer supports both synchronous and asynchronous requests:

Sync — blocking calls for sequential flows where each step depends on the previous result
Async — parallel calls via CompletableFuture for independent requests
JWT — token management with automatic Authorization: Bearer header injection
Test Data
Test data is stored in testData.json and loaded via DataHelper.loadTestData(). Random data generation is available via DataHelper.randomString() and DataHelper.randomEmail() for test isolation.

