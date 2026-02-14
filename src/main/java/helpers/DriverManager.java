package helpers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import config.TestConfig;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Manages WebDriver instances with thread-safe lifecycle control.
 *
 * Uses ThreadLocal to give each test thread its own browser instance,
 * enabling safe parallel test execution. Browser type and headless mode
 * are controlled via TestConfig.
 *
 * Supported browsers: Chrome (default), Firefox.
 * Driver binaries are downloaded automatically by WebDriverManager.
 *
 * Example usage:
 *
 *   WebDriver driver = DriverManager.getDriver();   // creates browser if needed
 *   driver.get("https://example.com");
 *   DriverManager.quitDriver();                      // closes browser and cleans up
 */
public class DriverManager {

    /**
     * Thread-local storage for WebDriver instances.
     * Each thread gets its own isolated browser, so parallel tests
     * don't interfere with each other.
     */
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    /**
     * Returns the WebDriver for the current thread.
     * If no driver exists yet, creates one using the browser and headless
     * settings from TestConfig.
     *
     * Call this instead of creating drivers directly — it ensures
     * one browser per thread and reuses it across calls within the same test.
     *
     * @return the current thread's WebDriver instance
     */
    public static WebDriver getDriver() {
        if (driver.get() == null) driver.set(createDriver());
        return driver.get();
    }

    /**
     * Creates a new WebDriver instance based on TestConfig settings.
     *
     * - Reads TestConfig.BROWSER to decide Chrome or Firefox
     * - Reads TestConfig.HEADLESS to enable/disable headless mode
     * - Uses WebDriverManager to auto-download the correct driver binary
     *
     * Chrome-specific flags:
     * - --no-sandbox: required for running in Docker/CI containers
     * - --disable-dev-shm-usage: prevents crashes in memory-limited environments
     *
     * @return a new WebDriver instance configured per TestConfig
     */
    private static WebDriver createDriver() {
        if ("firefox".equalsIgnoreCase(TestConfig.BROWSER)) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions opts = new FirefoxOptions();
            if (TestConfig.HEADLESS) opts.addArguments("--headless");
            return new FirefoxDriver(opts);
        }
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        if (TestConfig.HEADLESS) opts.addArguments("--headless");
        opts.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        return new ChromeDriver(opts);
    }

    /**
     * Closes the browser and removes the WebDriver from the current thread.
     * Call this in @AfterMethod or @AfterClass to clean up resources.
     *
     * Safe to call even if no driver exists — does nothing in that case.
     */
    public static void quitDriver() {
        if (driver.get() != null) { driver.get().quit(); driver.remove(); }
    }
}