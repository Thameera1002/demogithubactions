package com.githubactions;

import java.nio.charset.StandardCharsets;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Web Smoke Checks")
@Feature("Public site availability and title validation")
public class Demo {
    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        Allure.step("Chrome WebDriver initialized in headless mode");
    }

    @Test
    @Story("Google landing page")
    @Owner("qa-team")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Opens Google and validates that the page title contains Google.")
    public void testCase1(){
        validatePageTitleContains("https://www.google.com", "Google");
    }

    @Test
    @Story("GitHub landing page")
    @Owner("qa-team")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Opens GitHub and validates that the page title contains GitHub.")
    public void testCase2(){
        validatePageTitleContains("https://www.github.com", "GitHub");
    }

    @Test
    @Story("Stack Overflow landing page")
    @Owner("qa-team")
    @Severity(SeverityLevel.NORMAL)
    @Description("Opens Stack Overflow and validates that a non-empty title is returned.")
    public void testCase3(){
        validatePageTitleIsNotEmpty("https://www.stackoverflow.com");
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (!result.isSuccess()) {
            Allure.step("Test failed; collecting failure artifacts");
            attachText("Failed test", result.getName());
            attachText("Current URL", safeGetCurrentUrl());
            attachText("Page title", safeGetTitle());
            attachScreenshot();
            attachPageSource();
        }

        if (driver != null) {
            driver.quit();
        }
    }

    private void validatePageTitleContains(String url, String expectedTitlePart) {
        Allure.step("Navigate to " + url, () -> driver.get(url));

        String title = driver.getTitle();
        Allure.step("Capture page metadata");
        attachText("Current URL", driver.getCurrentUrl());
        attachText("Page title : ", title);
        attachScreenshot();

        Allure.step("Assert title contains expected value: " + expectedTitlePart, () ->
            Assert.assertTrue(title.contains(expectedTitlePart),
                "Expected title to contain '" + expectedTitlePart + "' but was '" + title + "'"));
    }

    private void validatePageTitleIsNotEmpty(String url) {
        Allure.step("Navigate to " + url, () -> driver.get(url));

        String title = driver.getTitle();
        Allure.step("Capture page metadata");
        attachText("Current URL", driver.getCurrentUrl());
        attachText("Page title", title);
        attachScreenshot();

        Allure.step("Assert page title is not empty", () ->
            Assert.assertFalse(title == null || title.trim().isEmpty(),
                "Expected non-empty title but got an empty value"));
    }

    private void attachScreenshot() {
        if (driver instanceof TakesScreenshot) {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.getLifecycle().addAttachment("Screenshot", "image/png", "png", screenshot);
        }
    }

    private void attachPageSource() {
        String source = driver.getPageSource();
        Allure.getLifecycle().addAttachment(
            "Page source",
            "text/html",
            "html",
            source.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void attachText(String name, String value) {
        Allure.addAttachment(name, value == null ? "N/A" : value);
    }

    private String safeGetCurrentUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (Exception ex) {
            return "Unavailable: " + ex.getMessage();
        }
    }

    private String safeGetTitle() {
        try {
            return driver.getTitle();
        } catch (Exception ex) {
            return "Unavailable: " + ex.getMessage();
        }
    }
}
