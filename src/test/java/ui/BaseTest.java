package ui;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import helpers.DriverManager;

/**
 * Abstract base class for all UI/WebDriver tests.
 *
 * Handles browser lifecycle automatically:
 * - Before each test: launches a new browser and maximizes the window
 * - After each test: closes the browser
 *
 * All UI test classes should extend this to get consistent setup/teardown
 * without duplicating browser management code.
 *
 * Example:
 *
 *   public class LoginTest extends BaseTest {
 *       @Test
 *       public void testLogin() {
 *           LoginPage loginPage = new LoginPage(driver);
 *           loginPage.open();
 *           loginPage.login("tomsmith", "SuperSecretPassword!");
 *           Assert.assertTrue(loginPage.isLoggedIn());
 *       }
 *   }
 */
public abstract class BaseTest {

    /**
     * The WebDriver instance for the current test.
     * Created fresh before each test method and closed after.
     * Accessible by all subclasses to pass to Page Objects.
     */
    protected WebDriver driver;

    /**
     * Runs before every test method.
     * Creates a new browser instance via DriverManager (thread-safe)
     * and maximizes the window for consistent element visibility.
     */
    @BeforeMethod
    public void setUp() {
        driver = DriverManager.getDriver();
        driver.manage().window().maximize();
    }

    /**
     * Runs after every test method, regardless of pass/fail.
     *
     * Always: closes the browser and releases the WebDriver to prevent
     * leftover browser processes.
     *
     * @param result the TestNG result object containing pass/fail status
     */
    @AfterMethod
    public void tearDown(ITestResult result) {
        DriverManager.quitDriver();
    }
}