package parsingLayer;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonMapper {


    //Parse LLM JSON response into simple String[]
    public static String[] parseStep(String response) {

        JSONObject o = new JSONObject(response);

        int stepId = o.optInt("stepId", 0);
        String stepDetails = o.optString("stepDetails", "");
        String actionType = o.optString("actionType", "");
        String action = o.optString("action", "");
        String selector = o.optString("selector", "");
        String value = o.optString("value", "");

        int generalWait = o.optInt("generalWait", 5);
        int screenshotWait = o.optInt("screenshotWait", 1);

        boolean screenshot = o.optBoolean("screenshot", true);
        boolean stopTesting = o.optBoolean("stopTesting", false);

        return new String[]{
                String.valueOf(stepId),
                stepDetails,
                actionType,
                action,
                selector,
                value,
                String.valueOf(generalWait),
                String.valueOf(screenshotWait),
                String.valueOf(screenshot),
                String.valueOf(stopTesting)
        };
    }

    //Build execution result JSON
    public static String buildResult(
            int stepId,boolean success,
            String message,String currentUrl)
    {
        JSONObject output = new JSONObject();
        output.put("type", "ExecutionResult");
        output.put("stepId", stepId);
        output.put("success", success);
        output.put("message",  message);
        output.put("currentUrl",  currentUrl);
        return output.toString();
    }

    //Build the FIRST message to the LLM (PlannerStart).
    public static String buildPlannerStart(
            String scenario, String currentUrl,
            String currentHtmlSlim, String currentScreenshotRef)
    {
        JSONObject output = new JSONObject();
        output.put("type", "PlannerStart");
        output.put("scenario", scenario);

        JSONArray rules = new JSONArray();
        rules.put("Return ONLY a single JSON object (no code fences, no explanation).");
        rules.put("ALL keys required.");
        rules.put("ONE step only.");
        rules.put("If finished: stopTesting=true and keep other fields empty/default.");
        output.put("rules", rules);

        output.put("requiredOutputSchema", getRequiredStepSchemaText());

        JSONObject state = new JSONObject();
        state.put("currentUrl",  currentUrl);
        state.put("currentHtmlSlim",  currentHtmlSlim);
        state.put("currentScreenshotRef",  currentScreenshotRef);
        output.put("currentState", state);

        return output.toString();
    }

     //Build LOOP message to the LLM after each step (PlannerUpdate).
     public static String buildPlannerUpdate(
             String scenario,
             int lastStepId,
             String lastStepDetails,
             boolean lastStepSuccess,
             String lastStepMessage,
             String currentUrl,
             String currentHtmlSlim,
             String currentScreenshotRef
     )
    {
        JSONObject output = new JSONObject();
        output.put("type", "PlannerUpdate");

        output.put("lastStepId", lastStepId);
        output.put("lastStepDetails", lastStepDetails);

        JSONObject result = new JSONObject();
        result.put("success", lastStepSuccess);
        result.put("message",  lastStepMessage);
        output.put("lastStepResult", result);

        //output.put("requiredOutputSchema", getRequiredStepSchemaText());

        JSONObject state = new JSONObject();
        state.put("currentUrl",  currentUrl);
        state.put("currentHtmlSlim", currentHtmlSlim);
        state.put("currentScreenshotRef",  currentScreenshotRef);
        output.put("currentState", state);
        output.put("scenario", scenario);

        return output.toString();
    }


    //The exact schema text we want the LLM to output each step.
    private static String getRequiredStepSchemaText() {
        return """
        Return ONLY a single JSON object (no code fences, no explanation).
        Schema (ALL keys required):
        {
          "stepId": 1,
          "stepDetails": "very short details about what we will do on this step",
          "actionType": "browserAction|elementAction|frameAction",
          "action": "click|type|clear|select|getText|getAttr|scroll|upload|navigate|refresh|back|maximize|getUrl|close|openNewWindow|getCustomTab|switchFrameById|switchFrameByName|switchFrameByIndex|switchToParent",
          "selector": "cssSelector:<...> OR xpath:<...> OR id:<...> (empty string allowed for pure browser actions like refresh/back/maximize also i accept only cssSelector,xpath,id,name,linkText,partialLinkText,className)",
          "value": "string (For Type in input field or for select from dropdown , empty if not needed)",
          "generalWait": 5,
          "screenshotWait": 1,
          "screenshot": true,
          "stopTesting": false
        }
        Rules:
        - Output valid JSON only.
        - ONE step only.
        - If finished, set stopTesting=true and keep other fields empty/defaults.
        """;
    }
}
