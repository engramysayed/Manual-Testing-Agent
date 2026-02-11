package helpers;

import drivers.WebDriverFactory;

public class OrchestratorHelper {

    private final WebDriverFactory driver;
    public OrchestratorHelper(WebDriverFactory driver){
        this.driver = driver;
    }



    public OrchestratorHelper navigateToRegisterPage() {

        return this;
    }






}
