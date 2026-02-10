package executionLayer;

import org.openqa.selenium.By;

public class SelectorParser {

    public static By toBy(String selector) {

        if (selector == null || selector.isBlank()) {
            throw new IllegalArgumentException("Selector is empty");
        }

        if (selector.startsWith("cssSelector:")) {
            return By.cssSelector(selector.substring("cssSelector:".length()));
        }

        if (selector.startsWith("xpath:")) {
            return By.xpath(selector.substring("xpath:".length()));
        }

        if (selector.startsWith("id:")) {
            return By.id(selector.substring("id:".length()));
        }

        if (selector.startsWith("name:")) {
            return By.name(selector.substring("name:".length()));
        }

        if (selector.startsWith("linkText:")) {
            return By.linkText(selector.substring("linkText:".length()));
        }

        if (selector.startsWith("partialLinkText:")) {
            return By.partialLinkText(selector.substring("partialLinkText:".length()));
        }

        if (selector.startsWith("className:")) {
            return By.className(selector.substring("className:".length()));
        }


        throw new IllegalArgumentException("Unsupported selector format: " + selector);
    }
}
