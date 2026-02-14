package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for the Dashboard / Secure Area page.
 *
 * This is the page users land on after a successful login.
 * Provides actions for logging out and reading page content.
 *
 * URL: /secure
 *
 * Example usage in a test:
 *
 *   DashboardPage dashboard = new DashboardPage(driver);
 *   Assert.assertEquals(dashboard.getHeaderText(), "Secure Area");
 *   Assert.assertTrue(dashboard.getFlashMessage().contains("You logged into"));
 *   dashboard.logout();
 */
public class DashboardPage extends BasePage {

    /** Logout link in the page content area. */
    private final By btnLogout    = By.cssSelector("a[href='/logout']");

    /** Main heading of the page (e.g., "Secure Area"). */
    private final By pageHeader   = By.tagName("h2");

    /** Flash notification banner shown after login/logout actions. */
    private final By flashMessage = By.id("flash");

    /**
     * @param driver the WebDriver instance (from DriverManager.getDriver())
     */
    public DashboardPage(WebDriver driver) { super(driver); }

    /**
     * Clicks the logout button to end the user session.
     * Redirects to the login page after logout.
     */
    public void logout() { click(btnLogout); }

    /**
     * Returns the text of the main page heading.
     *
     * @return the header text (e.g., "Secure Area")
     */
    public String getHeaderText() { return getText(pageHeader); }

    /**
     * Returns the text of the flash notification banner.
     * Flash messages appear after login ("You logged into a secure area!")
     * or logout ("You logged out of the secure area!").
     *
     * @return the flash message text including any whitespace/newlines
     */
    public String getFlashMessage() { return getText(flashMessage); }
}