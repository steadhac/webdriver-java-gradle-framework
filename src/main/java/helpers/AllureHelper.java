package helpers;

import java.io.ByteArrayInputStream;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import io.qameta.allure.Allure;

/**
 * Utility class for adding attachments and steps to Allure test reports.
 *
 * Provides static helper methods to enrich reports with screenshots,
 * text content, and named steps during test execution. Use these in
 * tests or page objects to capture useful debugging information.
 *
 * Example usage in a test:
 *
 *   AllureHelper.step("Navigate to login page");
 *   loginPage.open();
 *   AllureHelper.screenshot(driver, "Login page loaded");
 *   AllureHelper.text("Current URL", driver.getCurrentUrl());
 */
public class AllureHelper {

    /**
     * Captures a browser screenshot and attaches it to the Allure report.
     * The screenshot appears as an expandable image in the test's Attachments section.
     *
     * Typically called after key actions or on test failure to capture visual state.
     *
     * @param driver the WebDriver instance to capture the screenshot from
     * @param name   the label shown in the Allure report (e.g., "After login", "Error state")
     */
    public static void screenshot(WebDriver driver, String name) {
        byte[] img = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment(name, new ByteArrayInputStream(img));
    }

    /**
     * Attaches plain text content to the Allure report.
     * Useful for logging URLs, API responses, error messages, or any debug info.
     *
     * @param name    the label shown in the Allure report (e.g., "Response body", "Page source")
     * @param content the text content to attach
     */
    public static void text(String name, String content) {
        Allure.addAttachment(name, "text/plain", content);
    }

    /**
     * Adds a named step to the Allure report.
     * Steps appear as a sequential list in the report, showing the test flow.
     *
     * @param name the step description (e.g., "Click submit button", "Verify success message")
     */
    public static void step(String name) { Allure.step(name); }
}