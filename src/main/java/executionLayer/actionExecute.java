package executionLayer;

import drivers.WebDriverFactory;
import org.openqa.selenium.By;

import static java.lang.Integer.parseInt;

public class actionExecute {
    private final WebDriverFactory driver;

    public actionExecute(WebDriverFactory driver) {
        this.driver = driver;
    }

    public String checkAction(String option, String type,
                              String url, String tab, By locator, String value,
                              int generalWait) {
        return switch (option) {
            case "browserAction" -> browserAction(type, url, tab);

            case "elementAction" -> elementAction(type, locator, value, generalWait);

            case "frameAction" -> frameAction(type, value, locator, generalWait);


            default -> null;
        };
    }

    public String elementAction(String type, By locator, String value, int generalWait) {
        return switch (type) {
            case "click" -> driver.element().click(locator, generalWait);
            case "type" -> driver.element().type(locator, value, generalWait);
            case "clear" -> driver.element().clear(locator, generalWait);
            case "select" -> driver.element().selectFromDD(locator, value, generalWait);
            case "getText" -> driver.element().getText(locator, generalWait);
            case "getAttr" -> driver.element().getAttributeValue(locator, value, generalWait);
            case "scroll" -> driver.element().scrollToElement(locator); // no wait needed
            case "upload" -> driver.element().uploadFile(locator, value, generalWait);
            default -> null;
        };
    }

    public String browserAction(String type, String url, String tab) {
        return switch (type) {
            case "navigate" -> driver.browser().navigateToUrl(url);
            case "refresh" -> driver.browser().refreshPage();
            case "back" -> driver.browser().navigateBack();
            case "maximize" -> driver.browser().maximizeWindow();
            case "getUrl" -> driver.browser().getCurrentUrl();
            case "close" -> driver.browser().closeWindow();
            case "openNewWindow" -> driver.browser().newWindow();
            case "getCustomTab" -> driver.browser().customTab(tab);
            default -> null;
        };
    }

    public String frameAction(String frameType, String frameValue, By locator, int generalWait) {
        return switch (frameType) {
            case "switchFrameById" ->
                    driver.frames().switchToFrameByNameOrId(frameValue, generalWait);
            case "switchFrameByIndex" ->
                    driver.frames().switchToFrameByIndex(parseInt(frameValue), generalWait);
            case "switchFrameByName" ->
                     driver.frames().switchToFrameByElement(locator, generalWait);
            case "switchToParent" ->
                    driver.frames().switchToDefaultContent();
            default -> null;
        };
    }


}
