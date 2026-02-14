package pages;

import java.time.Duration;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.TestConfig;

/**
 * Abstract base class for all Page Objects in the framework.
 *
 * Provides three types of waits (implicit, explicit, fluent) and common
 * browser actions (click, type, getText). All page objects should extend
 * this class to inherit these capabilities.
 *
 * Wait strategy recommendations:
 * - Explicit waits (waitForVisible, waitForClickable) — use these by default
 * - Fluent waits (fluentWaitFor) — use when elements load unpredictably
 * - Implicit waits (setImplicitWait) — avoid mixing with explicit waits
 *
 * Example:
 *
 *   public class LoginPage extends BasePage {
 *       private By username = By.id("username");
 *       private By password = By.id("password");
 *       private By loginBtn = By.id("login");
 *
 *       public LoginPage(WebDriver driver) { super(driver); }
 *
 *       public void login(String user, String pass) {
 *           open("/login");
 *           type(username, user);
 *           type(password, pass);
 *           click(loginBtn);
 *       }
 *   }
 */
public abstract class BasePage {

    /** The WebDriver instance shared across all page actions. */
    protected WebDriver driver;

    /**
     * Standard explicit wait. Blocks up to TestConfig.TIMEOUT seconds
     * for a specific condition (visible, clickable, etc.) before failing.
     */
    protected WebDriverWait wait;

    /**
     * Fluent wait with custom polling. Checks every 500ms and ignores
     * NoSuchElementException during polling. Use for elements that
     * appear unpredictably or take variable time to load.
     */
    protected FluentWait<WebDriver> fluentWait;

    /**
     * Initializes the page with a WebDriver and sets up both wait strategies.
     *
     * @param driver the WebDriver instance (from DriverManager.getDriver())
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(TestConfig.TIMEOUT));
        this.fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(TestConfig.TIMEOUT))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class);
    }

    // ═══════════════════════════════════════════════════════════════
    // IMPLICIT WAIT
    // Sets a global timeout on ALL findElement() calls. Once set, every
    // element lookup waits up to this duration before throwing.
    // Warning: Avoid mixing with explicit waits — they can conflict
    // and cause unpredictable timeout behavior.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Sets the global implicit wait timeout. Applies to every findElement()
     * call made by Selenium until changed or cleared.
     *
     * @param seconds the maximum time to wait for elements to appear
     */
    protected void setImplicitWait(int seconds) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(seconds));
    }

    /**
     * Clears the implicit wait by setting it to 0 seconds.
     * Elements that are not immediately present will throw instantly.
     */
    protected void clearImplicitWait() {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    }

    // ═══════════════════════════════════════════════════════════════
    // EXPLICIT WAITS (recommended)
    // Per-element, condition-based waits. Each method blocks until a
    // specific condition is met or the timeout expires.
    // Preferred over implicit waits because they target specific
    // elements and conditions.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Waits until the element is present in the DOM AND visible on the page.
     * Use for elements that need to be seen by the user (text, images, etc.).
     *
     * @param locator the element locator (e.g., By.id("username"))
     * @return the visible WebElement
     */
    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until the element is visible AND enabled (can be clicked).
     * Use before clicking buttons, links, or any interactive element.
     *
     * @param locator the element locator
     * @return the clickable WebElement
     */
    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Waits until the element is no longer visible on the page.
     * Use for spinners, loading overlays, or disappearing messages.
     *
     * @param locator the element locator
     * @return true when the element is no longer visible
     */
    protected boolean waitForInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Waits until the element exists in the DOM (may not be visible).
     * Use for hidden elements or elements loaded via AJAX that aren't
     * displayed yet.
     *
     * @param locator the element locator
     * @return the present WebElement (may not be visible)
     */
    protected WebElement waitForPresence(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Waits until the element contains the specified text.
     * Use for verifying dynamic content has loaded (e.g., "Welcome, John").
     *
     * @param locator the element locator
     * @param text    the expected text (partial match)
     * @return true when the text is found in the element
     */
    protected boolean waitForText(By locator, String text) {
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    // ═══════════════════════════════════════════════════════════════
    // FLUENT WAIT
    // Like explicit waits but with custom polling interval and the
    // ability to ignore specific exceptions during polling.
    // Configured to poll every 500ms and ignore NoSuchElementException.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Waits for an element using the fluent wait strategy.
     * Polls every 500ms and ignores NoSuchElementException until
     * the element is found or the timeout expires.
     *
     * Use when elements appear at unpredictable intervals (e.g.,
     * lazy-loaded content, WebSocket-driven updates).
     *
     * @param locator the element locator
     * @return the found WebElement
     */
    protected WebElement fluentWaitFor(By locator) {
        return fluentWait.until(d -> d.findElement(locator));
    }

    // ═══════════════════════════════════════════════════════════════
    // COMMON ACTIONS
    // Reusable browser interactions that all page objects need.
    // Each action includes a wait to ensure the element is ready.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Navigates to a page by appending the path to TestConfig.BASE_URL.
     *
     * Example: open("/login") navigates to "https://the-internet.herokuapp.com/login"
     *
     * @param path the URL path (e.g., "/login", "/dashboard")
     */
    protected void open(String path) { driver.get(TestConfig.BASE_URL + path); }

    /**
     * Waits for the element to be clickable, then clicks it.
     *
     * @param locator the element locator
     */
    protected void click(By locator) { waitForClickable(locator).click(); }

    /**
     * Waits for the element to be visible, clears any existing text, then types new text.
     * Always clears first to avoid appending to existing values.
     *
     * @param locator the element locator (typically an input or textarea)
     * @param text    the text to type
     */
    protected void type(By locator, String text) {
        WebElement el = waitForVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

    /**
     * Waits for the element to be visible, then returns its text content.
     *
     * @param locator the element locator
     * @return the visible text of the element
     */
    protected String getText(By locator) { return waitForVisible(locator).getText(); }

    /**
     * Checks if an element is displayed on the page without waiting.
     * Returns false if the element doesn't exist or isn't visible.
     * Does NOT throw — safe to use in conditional logic.
     *
     * @param locator the element locator
     * @return true if the element exists and is displayed, false otherwise
     */
    protected boolean isDisplayed(By locator) {
        try { return driver.findElement(locator).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    /**
     * Returns the current page title from the browser.
     *
     * @return the page title string
     */
    public String getTitle() { return driver.getTitle(); }
}