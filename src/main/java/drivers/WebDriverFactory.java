package drivers;
import utils.LogsManager;
import handlers.AlertsHandler;
import handlers.BrowserHandler;
import handlers.ElementsHandler;
import handlers.FramesHandler;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ThreadGuard;
import utils.PropertyReader;
import validations.Validation;
import validations.Verification;

public class WebDriverFactory {

    public final static String browser = PropertyReader.getProperty("BROWSER_TYPE");
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public WebDriverFactory() {
        Browser browserType= Browser.valueOf(browser.toUpperCase());
        AbstractDriver abstractDriver = browserType.getDriverFactory();
        LogsManager.info("Starting Driver for Browser Type: " + browserType);
        WebDriver driver= ThreadGuard.protect(abstractDriver.createDriver());
        driverThreadLocal.set(driver);
    }

    public WebDriver get() {
        return driverThreadLocal.get();
    }

    public void quit(){
        driverThreadLocal.get().quit();
    }

    public ElementsHandler element(){
        return new ElementsHandler(get());
    }

    public BrowserHandler browser(){
        return new BrowserHandler(get());
    }

    public FramesHandler frames(){
        return new FramesHandler(get());
    }

    public AlertsHandler alerts(){
        return new AlertsHandler(get());
    }

    public Validation validation(){
        return new Validation(get());
    }

    public Verification verification(){
        return new Verification(get());
    }


}
