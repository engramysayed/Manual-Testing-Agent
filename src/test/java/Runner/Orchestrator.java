package Runner;

import aiLayer.LLMPlanner;
import executionLayer.SelectorParser;
import executionLayer.actionExecute;
import parsingLayer.JsonMapper;
import org.openqa.selenium.By;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;
import utils.HtmlSlimmer;
import utils.LogsManager;
import utils.ScreenshotService;

public class Orchestrator extends BaseOrchestrator {

    private int intOrDefault(String s, int fallback) {
        try {
            if (s == null || s.isBlank()) return fallback;
            return Integer.parseInt(s);
        } catch (Exception e) {
            return fallback;
        }
    }

    @Test
    public void runScenario() throws Exception {

        // 1) Dependencies
        actionExecute executor = new actionExecute(driver);
        LLMPlanner planner = new LLMPlanner(runFolder);

        // 2) Scenario (later you can make this DataProvider)
        String scenario =
                "Test Google search: type 'OpenAI' in search box and click search, then stop.";

        // 3) Initial state
        String currentUrl = safeGetUrl();
        String currentHtmlSlim = HtmlSlimmer.slim(safeGetHtml(), HTML_MAX_CHARS);

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

            // Ask LLM
            String llmResponse = planner.getNextStep(stepCounter, stateJson);

            // Parse step
            String[] step = JsonMapper.parseStep(llmResponse);

            int stepId = intOrDefault(step[0], stepCounter);
            String stepDetails = step[1];
            String actionType = step[2];
            String action = step[3];
            String selector = step[4];
            String value = step[5];

            int generalWait = intOrDefault(step[6], DEFAULT_WAIT);
            int screenshotWait = intOrDefault(step[7], DEFAULT_SCREENSHOT_WAIT);

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
                // selector -> By
                By locator = null;
                if (selector != null && !selector.isBlank()) {
                    locator = SelectorParser.toBy(selector);
                }

                // browser navigate uses value as url
                String urlParam = "";
                String tabParam = "";

                if ("browserAction".equalsIgnoreCase(actionType) && "navigate".equalsIgnoreCase(action)) {
                    urlParam = value;
                }

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

                // Screenshot (no double wait block)
                if (screenshot) {
                    if (screenshotWait > 0) Thread.sleep(screenshotWait * 1000L);
                    ScreenshotService.capture(driver.get(), runFolder, stepId);
                }

            } catch (Exception e) {
                success = false;
                message = e.getMessage();
                LogsManager.error("Execution error: " + message);
            }

            // Build update state
            currentUrl = safeGetUrl();
            currentHtmlSlim = HtmlSlimmer.slim(safeGetHtml(), HTML_MAX_CHARS);

            stateJson = JsonMapper.buildPlannerUpdate(
                    scenario,
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
    }

    @AfterSuite(alwaysRun = true)
    public void teardown() {
        try {
            if (driver != null) driver.quit();
        } catch (Exception ignored) {}
    }

    private String safeGetUrl() {
        try {
            String url = driver.get().getCurrentUrl();
            return url == null ? "" : url;
        } catch (Exception e) {
            return "";
        }
    }

    private String safeGetHtml() {
        try {
            String html = driver.get().getPageSource();
            return html == null ? "" : html;
        } catch (Exception e) {
            return "";
        }
    }
}
