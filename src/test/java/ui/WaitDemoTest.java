package ui;

import java.time.Duration;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

/**
 * Demonstrates the three Selenium wait strategies side by side.
 *
 * Each test uses a different wait approach to handle the same problem:
 * elements that are not immediately available in the DOM. This class
 * serves as a reference for when to use each strategy.
 *
 * Summary:
 * - Implicit Wait: simple but applies globally, avoid mixing with others
 * - Explicit Wait: recommended default, condition-based, per-element
 * - Fluent Wait: most flexible, custom polling and exception handling
 *
 * Target pages:
 * - /login (static page — for implicit wait demo)
 * - /dynamic_loading/1 (element hidden until button clicked — for explicit/fluent demos)
 */
@Epic("Wait Strategies") @Feature("Explicit vs Implicit Waits")
public class WaitDemoTest extends BaseTest {

    /**
     * Demonstrates IMPLICIT WAIT.
     *
     * How it works:
     * - Sets a global 10-second timeout on ALL findElement() calls
     * - If an element isn't found immediately, Selenium polls the DOM
     *   until the element appears or the timeout expires
     * - Applies to every findElement() call from this point forward
     *
     * Pros: Simple one-line setup
     * Cons: Applies globally, can't target specific elements,
     *        conflicts with explicit waits, hides slow page loads
     *
     * The timeout is cleared to 0 at the end to avoid affecting other tests.
     */
    @Test(description = "IMPLICIT WAIT — global, applies to all findElement")
    @Severity(SeverityLevel.NORMAL) @Story("Implicit Wait")
    public void testImplicitWait() {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get("https://the-internet.herokuapp.com/login");
        Assert.assertTrue(driver.findElement(By.id("username")).isDisplayed());
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    }

    /**
     * Demonstrates EXPLICIT WAIT (recommended approach).
     *
     * How it works:
     * - Creates a WebDriverWait with a 15-second timeout
     * - Waits for a SPECIFIC CONDITION on a SPECIFIC ELEMENT
     * - Only applies where you use it — no global side effects
     *
     * Flow:
     * 1. Navigate to the dynamic loading page
     * 2. Wait for the "Start" button to be clickable, then click it
     * 3. Wait for the result element to become visible (hidden by default)
     * 4. Assert the result text contains "Hello World"
     *
     * Pros: Precise, condition-based, per-element, no global impact
     * Cons: Slightly more verbose than implicit
     *
     * This is the recommended wait strategy for most situations.
     */
    @Test(description = "EXPLICIT WAIT — per-element, condition-based")
    @Severity(SeverityLevel.CRITICAL) @Story("Explicit Wait")
    public void testExplicitWait() {
        driver.get("https://the-internet.herokuapp.com/dynamic_loading/1");
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(15));
        w.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#start button"))).click();
        WebElement result = w.until(ExpectedConditions.visibilityOfElementLocated(By.id("finish")));
        Assert.assertTrue(result.getText().contains("Hello World"));
    }

    /**
     * Demonstrates FLUENT WAIT.
     *
     * How it works:
     * - Like explicit wait but with full control over polling behavior
     * - Polls every 500ms (configurable) instead of the default 500ms
     * - Ignores NoSuchElementException during polling so it doesn't
     *   fail when the element doesn't exist yet
     * - Accepts a lambda/function that returns null to keep waiting
     *   or a value to stop waiting
     *
     * Flow:
     * 1. Navigate to the dynamic loading page
     * 2. Poll every 500ms for the "Start" button, click when found
     * 3. Poll for the result element — return it only when displayed,
     *    return null to keep polling if not yet visible
     * 4. Assert the result text contains "Hello World"
     *
     * Pros: Custom polling interval, custom exception ignoring,
     *        complex conditions via lambda logic
     * Cons: Most verbose, overkill for simple waits
     *
     * Use when elements load unpredictably or you need fine-grained
     * control over polling (e.g., slow APIs, WebSocket-driven content).
     */
    @Test(description = "FLUENT WAIT — custom polling + exception ignoring")
    @Severity(SeverityLevel.NORMAL) @Story("Fluent Wait")
    public void testFluentWait() {
        driver.get("https://the-internet.herokuapp.com/dynamic_loading/1");
        FluentWait<org.openqa.selenium.WebDriver> fw = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(15))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class);
        fw.until(d -> d.findElement(By.cssSelector("#start button"))).click();
        WebElement result = fw.until(d -> {
            WebElement el = d.findElement(By.id("finish"));
            return el.isDisplayed() ? el : null;
        });
        Assert.assertTrue(result.getText().contains("Hello World"));
    }
}