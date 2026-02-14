package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page Object for the Login page.
 *
 * Handles navigation to the login form, entering credentials,
 * and submitting. Also provides methods to check login success
 * or read error/success flash messages.
 *
 * URL: /login
 *
 * Example usage in a test:
 *
 *   LoginPage loginPage = new LoginPage(driver);
 *   loginPage.open();
 *   loginPage.login("tomsmith", "SuperSecretPassword!");
 *   Assert.assertTrue(loginPage.isLoggedIn());
 */
public class LoginPage extends BasePage {

    /** Username text input field. */
    private final By inputUsername = By.id("username");

    /** Password text input field. */
    private final By inputPassword = By.id("password");

    /** Submit button that triggers the login form. */
    private final By btnSubmit     = By.cssSelector("button[type='submit']");

    /** Flash notification banner for success/error messages after login attempts. */
    private final By flashMessage  = By.id("flash");

    /** Main page heading — changes to "Secure Area" after successful login. */
    private final By pageHeader    = By.tagName("h2");

    /**
     * @param driver the WebDriver instance (from DriverManager.getDriver())
     */
    public LoginPage(WebDriver driver) { super(driver); }

    /**
     * Navigates to the login page at /login.
     */
    public void open() { super.open("/login"); }

    /**
     * Fills in the username and password fields and clicks the submit button.
     * After this call, the page will either redirect to the dashboard
     * (success) or show a flash error message (failure).
     *
     * @param username the username to enter (e.g., "tomsmith")
     * @param password the password to enter (e.g., "SuperSecretPassword!")
     */
    public void login(String username, String password) {
        type(inputUsername, username);
        type(inputPassword, password);
        click(btnSubmit);
    }

    /**
     * Returns the text of the flash notification banner.
     * Shows success ("You logged into a secure area!") or
     * error ("Your username is invalid!") after a login attempt.
     *
     * @return the flash message text
     */
    public String getFlashMessage() { return getText(flashMessage); }

    /**
     * Checks whether the login was successful by looking for "Secure Area"
     * in the page heading.
     *
     * @return true if the user is on the authenticated dashboard page
     */
    public boolean isLoggedIn() { return getText(pageHeader).contains("Secure Area"); }
}