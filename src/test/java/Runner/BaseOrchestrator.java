package Runner;

import drivers.WebDriverFactory;
import drivers.WebDriverProvider;
import org.openqa.selenium.WebDriver;
import utils.PropertyReader;

public class BaseOrchestrator  implements WebDriverProvider {
        protected WebDriverFactory driver;
        protected int  HTML_MAX_CHARS;

        @Override
        public WebDriver getWebDriver() {
            return driver.get();
        }


        public void initialize() {
            PropertyReader.loadProperties();
            HTML_MAX_CHARS = Integer.parseInt(PropertyReader.getProperty("HTML_MAX_CHARS"));
        }




}
