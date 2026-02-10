package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitHandler {

    private final WebDriver driver;

    public WaitHandler(WebDriver driver) {
        this.driver = driver;
    }

    private WebDriverWait wait(int seconds) {
        int s = Math.max(1, seconds); // sanitize (AI might send 0)
        return new WebDriverWait(driver, Duration.ofSeconds(s));
    }

    public WebElement waitForElementToBeClickable(By locator, int waitSeconds) {
        try {
            return wait(waitSeconds).until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException e) {
            LogsManager.error("Element not clickable within " + waitSeconds + "s: " + locator);
            return null;
        }
    }

    public WebElement waitForElementToBeVisible(By locator, int waitSeconds) {
        try {
            return wait(waitSeconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            LogsManager.error("Element not visible within " + waitSeconds + "s: " + locator);
            return null;
        }
    }

    public Alert waitForAlert(int waitSeconds) {
        try {
            return wait(waitSeconds).until(ExpectedConditions.alertIsPresent());
        } catch (TimeoutException e) {
            LogsManager.error("No alert present within " + waitSeconds + "s");
            return null;
        }
    }

    public void waitFrameByIndex(int index, int waitSeconds) {
        try {
            wait(waitSeconds).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(index));
        } catch (Exception e) {
            LogsManager.error("Failed to switch to frame index " + index + " within " + waitSeconds + "s");
        }
    }

    public void waitFrameByNameOrId(String nameOrId, int waitSeconds) {
        try {
            wait(waitSeconds).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(nameOrId));
        } catch (Exception e) {
            LogsManager.error("Failed to switch to frame name/id " + nameOrId + " within " + waitSeconds + "s");
        }
    }

    public void waitFrameByElement(By locator, int waitSeconds) {
        try {
            wait(waitSeconds).until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
        } catch (Exception e) {
            LogsManager.error("Failed to switch to frame by element " + locator + " within " + waitSeconds + "s");
        }
    }
}

