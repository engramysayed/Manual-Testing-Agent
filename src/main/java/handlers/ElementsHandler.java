package handlers;

import utils.LogsManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import utils.WaitHandler;

import java.io.File;

public class ElementsHandler {
    private final WebDriver driver;
    private final WaitHandler waitHandler;

    public ElementsHandler(WebDriver driver) {
        this.driver = driver;
        this.waitHandler = new WaitHandler(driver);
    }

    public String scrollToElement(By locator) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'auto', block: 'center', inline: 'center'});",
                    findElement(locator)
            );
            return "true";
        } catch (Exception e) {
            LogsManager.error("Failed to scroll to element: " + locator);
            return "false";
        }
    }

    public WebElement findElement(By locator) {
        return driver.findElement(locator);
    }

    public String selectFromDD(By locator, String option, int generalWait) {
        try {
            scrollToElement(locator);
            waitHandler.waitForElementToBeVisible(locator, generalWait);
            new Select(findElement(locator)).selectByValue(option);
            LogsManager.info("Option selected successfully from dropdown: " + locator + " option: " + option);
            return "true";
        } catch (Exception e) {
            LogsManager.error("Failed to select option from dropdown: " + locator + " " + e.getMessage());
            return "false";
        }
    }

    public String uploadFile(By locator, String path, int generalWait){
        try {
            String filePath = System.getProperty("user.dir") + File.separator + path;
            waitHandler.waitForElementToBeVisible(locator, generalWait);
            findElement(locator).sendKeys(filePath);
            LogsManager.info("File uploaded successfully: " + path);
            return "true";
        } catch (Exception e) {
            LogsManager.error("Failed to upload file: " + path + " " + e.getMessage());
            return "false";
        }
    }

    public String click(By locator, int generalWait) {
        try {
            scrollToElement(locator);
            waitHandler.waitForElementToBeVisible(locator, generalWait);
            findElement(locator).click();
            return "true";
        } catch (Exception e) {
            LogsManager.error("Failed to click element: " + locator + " " + e.getMessage());
            return "false";
        }
    }

    public String type(By locator, String text, int generalWait) {
        try {
            waitHandler.waitForElementToBeVisible(locator, generalWait);
            WebElement el = findElement(locator);
            el.clear();
            el.sendKeys(text);
            LogsManager.info("Text typed into element: " + locator);
            return "true";
        } catch (Exception e) {
            LogsManager.error("Failed to type into element: " + locator + " " + e.getMessage());
            return "false";
        }
    }

    public String getText(By locator, int generalWait) {
        try {
            waitHandler.waitForElementToBeVisible(locator, generalWait);
            String text = findElement(locator).getText();
            return (text != null && !text.isEmpty()) ? text : null;
        } catch (Exception e) {
            LogsManager.error("Failed to get text from: " + locator + " " + e.getMessage());
            return "false";
        }
    }

    public String clear(By locator, int generalWait) {
        try {
            waitHandler.waitForElementToBeVisible(locator, generalWait);
            findElement(locator).clear();
            return "true";
        } catch (Exception e) {
            LogsManager.error("Failed to clear element: " + locator + " " + e.getMessage());
            return "false";
        }
    }

    public String getAttributeValue(By locator, String attributeName, int generalWait) {
        try {
            waitHandler.waitForElementToBeVisible(locator, generalWait);
            String val = findElement(locator).getAttribute(attributeName);
            return (val != null && !val.isEmpty()) ? val : "";
        } catch (Exception e) {
            LogsManager.error("Failed to get attribute value: " + locator + " " + e.getMessage());
            return "false";
        }
    }
}
