package Runner;

import aiLayer.LLMPlanner;
import drivers.WebDriverFactory;
import executionLayer.actionExecute;
import executionLayer.SelectorParser;
import parsingLayer.JsonMapper;

import org.openqa.selenium.By;
import utils.HtmlSlimmer;
import utils.LogsManager;
import utils.PropertyReader;
import utils.ScreenshotService;

import java.nio.file.Path;

public class TestRunner {
    public static int generalWait;
    // You can move this to properties later
    private static final int HTML_MAX_CHARS = 20000;

    public static void main(String[] args) throws Exception {

        // Load properties first
        PropertyReader.loadProperties();

        // 1) Start driver
        WebDriverFactory driver = new WebDriverFactory();

        // 2) Create executor wrapper
        actionExecute executor = new actionExecute(driver);

        // 3) Planner run folder
        Path runFolder = Path.of(System.getProperty("user.dir"), "test-output", "run_1");
        LLMPlanner planner = new LLMPlanner(runFolder);

        // 4) Build initial state (start message)
        String currentUrl = safeGetUrl(driver);
        String currentHtmlSlim = HtmlSlimmer.slim(safeGetHtml(driver), HTML_MAX_CHARS);

        String scenario =
                "Write your scenario here. Example: " +
                        "Test Google search: type 'OpenAI' in search box and click search, then stop.";

        String stateJson = JsonMapper.buildPlannerStart(
                scenario,
                currentUrl,
                currentHtmlSlim,
                "step_0.png"
        );

        int stepCounter = 1;
        boolean stopTesting = false;

        while (!stopTesting) {

            LogsManager.info("==========================================");
            LogsManager.info("Requesting next step from LLM. stepId=" + stepCounter);

            // 5) Ask the LLM for ONE step (JSON)
            String llmResponse = planner.getNextStep(String.valueOf(stepCounter), stateJson);

            // 6) Parse the returned step JSON
            String[] step = JsonMapper.parseStep(llmResponse);

            int stepId = Integer.parseInt(step[0]);
            String stepDetails = step[1];
            String actionType = step[2];
            String action = step[3];
            String selector = step[4];
            String value = step[5];
            int generalWait = Integer.parseInt(step[6]);     // currently not injected into WaitHandler, keep for later
            int screenshotWait = Integer.parseInt(step[7]);
            boolean screenshot = Boolean.parseBoolean(step[8]);
            stopTesting = Boolean.parseBoolean(step[9]);

            LogsManager.info("LLM Step: " + stepId + " | " + stepDetails);
            LogsManager.info("actionType=" + actionType + ", action=" + action + ", selector=" + selector);

            if (stopTesting) {
                LogsManager.info("stopTesting=true -> Ending loop.");
                break;
            }

            boolean success = true;
            String message = "OK";

            try {
                // 7) Convert selector -> By (only needed for element actions / some frame actions)
                By locator = null;
                if (selector != null && !selector.isBlank()) {
                    locator = SelectorParser.toBy(selector);
                }

                // 8) Execute the action (your existing router)
                // browserAction:
                //   - url must come in "value" if action is navigate (so we pass it as url param)
                // frameAction:
                //   - frame target comes in "value"
                // elementAction:
                //   - uses locator + value
                String urlParam = "";
                String tabParam = "";

                if ("browserAction".equalsIgnoreCase(actionType) && "navigate".equalsIgnoreCase(action)) {
                    urlParam = value; // value carries the url
                }

                // IMPORTANT: for frameAction your code uses frameAction(type, value)
                // So value should contain frame id/name/index etc.
                String result = executor.checkAction(
                        actionType,
                        action,
                        urlParam,
                        tabParam,
                        locator,
                        value,
                        generalWait
                );

                if (result == null || "false".equalsIgnoreCase(result)) {
                    success = false;
                    message = "Action returned failure: " + result;
                } else {
                    message = "Action executed: " + result;
                }

                // 9) Wait after action before screenshot if needed
                if (screenshot && screenshotWait > 0) {
                    Thread.sleep(screenshotWait * 1000L);
                    if (screenshot ==true && screenshotWait > 0) {
                        Thread.sleep(screenshotWait * 1000L);
                        ScreenshotService.capture(driver.get(), runFolder, stepId);
                    }
                }

            } catch (Exception e) {
                success = false;
                message = e.getMessage();
                LogsManager.error("Execution error: " + message);
            }

            // 10) Capture new state for PlannerUpdate
            currentUrl = safeGetUrl(driver);
            currentHtmlSlim = HtmlSlimmer.slim(safeGetHtml(driver), HTML_MAX_CHARS);

            stateJson = JsonMapper.buildPlannerUpdate(
                    stepId,
                    stepDetails,
                    success,
                    message,
                    currentUrl,
                    currentHtmlSlim,
                    "step_" + stepId + ".png"
            );

            stepCounter++;
        }

        // Quit driver
        driver.quit();
        LogsManager.info("Runner finished.");
    }

    private static String safeGetUrl(WebDriverFactory driver) {
        try {
            String url = driver.get().getCurrentUrl();
            return url == null ? "" : url;
        } catch (Exception e) {
            return "";
        }
    }

    private static String safeGetHtml(WebDriverFactory driver) {
        try {
            String html = driver.get().getPageSource();
            return html == null ? "" : html;
        } catch (Exception e) {
            return "";
        }
    }
}
