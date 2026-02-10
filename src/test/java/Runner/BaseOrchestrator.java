package Runner;

import drivers.WebDriverFactory;
import drivers.WebDriverProvider;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeSuite;
import utils.PropertyReader;

import java.nio.file.Path;

public class BaseOrchestrator implements WebDriverProvider {

    protected WebDriverFactory driver;
    protected int HTML_MAX_CHARS;
    protected int DEFAULT_WAIT;
    protected int DEFAULT_SCREENSHOT_WAIT;
    protected Path runFolder;

    @Override
    public WebDriver getWebDriver() {
        return driver.get();
    }

    @BeforeSuite
    public void initialize() {
        PropertyReader.loadProperties();

        HTML_MAX_CHARS = Integer.parseInt(PropertyReader.getProperty("HTML_MAX_CHARS"));
        DEFAULT_WAIT = Integer.parseInt(PropertyReader.getProperty("DEFAULT_WAIT"));
        DEFAULT_SCREENSHOT_WAIT = Integer.parseInt(PropertyReader.getProperty("DEFAULT_SCREENSHOT_WAIT"));

        runFolder = Path.of(System.getProperty("user.dir"), "test-output", "run_1");

        driver = new WebDriverFactory();
    }
}
