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
import pages.DashboardPage;
import pages.LoginPage;

/**
 * Tests for the Dashboard / Secure Area page.
 *
 * All tests start from an authenticated state — the init() method
 * logs in using credentials from testData.json before each test.
 * Browser setup and teardown are inherited from BaseTest.
 *
 * Test data: src/test/resources/testData.json (validUser object)
 * Target page: /secure (after login redirect)
 */
@Epic("Dashboard") @Feature("Secure Area")
public class DashboardTest extends BaseTest {

    /** Page object for the login page — used in init() to authenticate. */
    private LoginPage loginPage;

    /** Page object for the dashboard — used by tests to interact with the secure area. */
    private DashboardPage dashboardPage;

    /**
     * Runs before each test to set up an authenticated session.
     *
     * Flow:
     * 1. Creates page objects for login and dashboard
     * 2. Loads valid credentials from testData.json
     * 3. Navigates to /login and submits the credentials
     * 4. After this, the browser is on the dashboard page, ready for tests
     */
    @BeforeMethod
    public void init() {
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
        JsonObject u = DataHelper.loadTestData("testData.json").getAsJsonObject("validUser");
        loginPage.open();
        loginPage.login(u.get("username").getAsString(), u.get("password").getAsString());
    }

    /**
     * Verifies the dashboard header displays "Secure Area" after login.
     * Confirms the user landed on the correct page.
     */
    @Test(description = "Secure area header visible")
    @Severity(SeverityLevel.NORMAL) @Story("Dashboard Display")
    public void testHeader() {
        Assert.assertTrue(dashboardPage.getHeaderText().contains("Secure Area"));
    }

    /**
     * Verifies the logout flow works correctly.
     *
     * Flow:
     * 1. Clicks the logout button on the dashboard
     * 2. Redirects back to the login page
     * 3. Asserts the flash message confirms successful logout
     */
    @Test(description = "Logout")
    @Severity(SeverityLevel.CRITICAL) @Story("Logout")
    public void testLogout() {
        dashboardPage.logout();
        Assert.assertTrue(loginPage.getFlashMessage().contains("You logged out of the secure area!"));
    }
}