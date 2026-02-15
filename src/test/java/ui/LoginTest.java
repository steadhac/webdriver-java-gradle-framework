package ui;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import helpers.DataHelper;
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
public class LoginTest extends BaseTest {

    private LoginPage loginPage;
    private JsonObject data;

    @BeforeMethod
    public void init() {
        loginPage = new LoginPage(driver);
        data = DataHelper.loadTestData("testData.json");
        loginPage.open();
    }

    @Test(description = "Valid login")
    public void testValidLogin() {
        JsonObject u = data.getAsJsonObject("validUser");
        loginPage.login(u.get("username").getAsString(), u.get("password").getAsString());
        // Re-find or use a method that checks login status using a fresh element
        Assert.assertTrue(loginPage.isLoggedIn());
    }

    @Test(description = "Invalid login")
    public void testInvalidLogin() {
        JsonObject u = data.getAsJsonObject("invalidUser");
        loginPage.login(u.get("username").getAsString(), u.get("password").getAsString());
        // Re-find the flash message element after login attempt
        Assert.assertTrue(loginPage.getFlashMessage().contains("Your username is invalid!"));
    }

    @Test(description = "Empty username")
    public void testEmptyUsername() {
        JsonObject u = data.getAsJsonObject("validUser");
        loginPage.login("", u.get("password").getAsString());
        Assert.assertTrue(loginPage.getFlashMessage().contains("Your username is invalid!"));
    }

    @Test(description = "Empty password")
    public void testEmptyPassword() {
        JsonObject u = data.getAsJsonObject("validUser");
        loginPage.login(u.get("username").getAsString(), "");
        Assert.assertTrue(loginPage.getFlashMessage().contains("Your password is invalid!"));
    }
}