package config;

/**
 * Central configuration for all tests in the framework.
 *
 * Values are loaded from JVM system properties (-D flags) at runtime,
 * with sensible defaults for local development. Override any value
 * when running from the command line or CI:
 *
 *   gradle test -DbaseUrl=https://staging.example.com -Dbrowser=firefox -Dheadless=false
 */
public class TestConfig {

    /**
     * Base URL for UI/WebDriver tests.
     * Override with: -DbaseUrl=https://your-app.com
     * Default: https://the-internet.herokuapp.com
     */
    public static final String BASE_URL = System.getProperty("baseUrl", "https://the-internet.herokuapp.com");

    /**
     * Base URL for API/REST Assured tests.
     * Used by ApiClient as the root for all endpoint paths.
     * Override with: -DapiBaseUrl=https://your-api.com
     * Default: https://jsonplaceholder.typicode.com
     */
    public static final String API_BASE_URL = System.getProperty("apiBaseUrl", "https://jsonplaceholder.typicode.com");

    /**
     * Global timeout in seconds for WebDriver waits (explicit and implicit).
     * Override with: -Dtimeout=30
     * Default: 10
     */
    public static final int TIMEOUT = Integer.parseInt(System.getProperty("timeout", "10"));

    /**
     * Browser to use for WebDriver tests (chrome, firefox, edge, safari).
     * Override with: -Dbrowser=firefox
     * Default: chrome
     */
    public static final String BROWSER = System.getProperty("browser", "chrome");

    /**
     * Whether to run the browser in headless mode (no visible window).
     * Set to true for CI pipelines, false for local debugging.
     * Override with: -Dheadless=false
     * Default: true
     */
    public static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("headless", "true"));
}