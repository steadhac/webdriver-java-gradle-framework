package ui;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import helpers.DataHelper;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import pages.LoginPage;

/**
 * Tests for the Login page authentication flow.
 *
 * Covers positive and negative login scenarios including valid credentials,
 * invalid credentials, empty username, and empty password.
 * Test data is loaded from testData.json to keep credentials out of code.
 * Browser setup, teardown, and failure screenshots are inherited from BaseTest.
 *
 * Test data: src/test/resources/testData.json (validUser and invalidUser objects)
 * Target page: /login
 */
@Epic("Authentication") @Feature("Login")
public class LoginTest extends BaseTest {

    /** Page object for interacting with the login form. */
    private LoginPage loginPage;

    /** Test data loaded from testData.json containing user credentials. */
    private JsonObject data;

    /**
     * Runs before each test.
     * Creates the LoginPage object, loads test data, and navigates to /login.
     * Each test starts on a fresh login page with no prior state.
     */
    @BeforeMethod
    public void init() {
        loginPage = new LoginPage(driver);
        data = DataHelper.loadTestData("testData.json");
        loginPage.open();
    }

    /**
     * Verifies successful login with valid credentials.
     * Uses the "validUser" object from testData.json.
     * Asserts the page redirects to the Secure Area after login.
     */
    @Test(description = "Valid login")
    @Severity(SeverityLevel.CRITICAL) @Story("Valid Login")
    public void testValidLogin() {
        JsonObject u = data.getAsJsonObject("validUser");
        loginPage.login(u.get("username").getAsString(), u.get("password").getAsString());
        Assert.assertTrue(loginPage.isLoggedIn());
    }

    /**
     * Verifies login is rejected with invalid credentials.
     * Uses the "invalidUser" object from testData.json.
     * Asserts the flash message shows "Your username is invalid!"
     */
    @Test(description = "Invalid login")
    @Severity(SeverityLevel.CRITICAL) @Story("Invalid Login")
    public void testInvalidLogin() {
        JsonObject u = data.getAsJsonObject("invalidUser");
        loginPage.login(u.get("username").getAsString(), u.get("password").getAsString());
        Assert.assertTrue(loginPage.getFlashMessage().contains("Your username is invalid!"));
    }

    /**
     * Verifies login is rejected when username is left empty.
     * Submits an empty username with a valid password.
     * Asserts the flash message shows "Your username is invalid!"
     */
    @Test(description = "Empty username")
    @Severity(SeverityLevel.NORMAL) @Story("Validation")
    public void testEmptyUsername() {
        JsonObject u = data.getAsJsonObject("validUser");
        loginPage.login("", u.get("password").getAsString());
        Assert.assertTrue(loginPage.getFlashMessage().contains("Your username is invalid!"));
    }

    /**
     * Verifies login is rejected when password is left empty.
     * Submits a valid username with an empty password.
     * Asserts the flash message shows "Your password is invalid!"
     */
    @Test(description = "Empty password")
    @Severity(SeverityLevel.NORMAL) @Story("Validation")
    public void testEmptyPassword() {
        JsonObject u = data.getAsJsonObject("validUser");
        loginPage.login(u.get("username").getAsString(), "");
        Assert.assertTrue(loginPage.getFlashMessage().contains("Your password is invalid!"));
    }
}