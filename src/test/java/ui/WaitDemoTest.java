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
public class WaitDemoTest extends BaseTest {

    @Test(description = "IMPLICIT WAIT — global, applies to all findElement")
    public void testImplicitWait() {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get("https://the-internet.herokuapp.com/login");
        Assert.assertTrue(driver.findElement(By.id("username")).isDisplayed());
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    }

    @Test(description = "EXPLICIT WAIT — per-element, condition-based")
    public void testExplicitWait() {
        driver.get("https://the-internet.herokuapp.com/dynamic_loading/1");
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(15));
        w.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#start button"))).click();
        WebElement result = w.until(ExpectedConditions.visibilityOfElementLocated(By.id("finish")));
        Assert.assertTrue(result.getText().contains("Hello World"));
    }

    @Test(description = "FLUENT WAIT — custom polling + exception ignoring")
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