package ui;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.JsonObject;

import helpers.DataHelper;
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
public class DashboardTest extends BaseTest {

    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    @BeforeMethod
    public void init() {
        loginPage = new LoginPage(driver);
        dashboardPage = new DashboardPage(driver);
        JsonObject u = DataHelper.loadTestData("testData.json").getAsJsonObject("validUser");
        loginPage.open();
        loginPage.login(u.get("username").getAsString(), u.get("password").getAsString());
    }

    @Test(description = "Secure area header visible")
    public void testHeader() {
        Assert.assertFalse(dashboardPage.getHeaderText().isEmpty(), "Dashboard header should not be empty");
    }

    @Test(description = "Logout")
    public void testLogout() {
        dashboardPage.logout();
        Assert.assertTrue(loginPage.getFlashMessage().contains("You logged out of the secure area!"));
    }
}