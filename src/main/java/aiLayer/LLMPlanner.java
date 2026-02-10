package aiLayer;

import utils.FilesManager;

import java.nio.file.Path;

public class LLMPlanner {

    private final Path runFolder;
    private final LLMClient llmClient;

    public LLMPlanner(Path runFolder) {
        this.runFolder = runFolder;
        this.llmClient = new LLMClient();
        FilesManager.createDirectory(runFolder.resolve("planner").toString());
    }


    public String getNextStep(int stepId, String plannerMessageJson) throws Exception {

        String prompt = buildPrompt(plannerMessageJson);

        Path promptFile = runFolder.resolve("planner").resolve("step_" + stepId + "_prompt.txt");
        FilesManager.writeFile(promptFile, prompt);

        String responseText = llmClient.call(prompt, 1200);

        Path responseFile = runFolder.resolve("planner").resolve("step_" + stepId + "_response.json");
        FilesManager.writeFile(responseFile, responseText);

        return responseText;
    }

    private String buildPrompt(String plannerMessageJson) {
        return """
You are a test automation planner.

You will receive a JSON message (PlannerStart or PlannerUpdate) that includes:
- the scenario
- the current state (url + slim html)
- and the REQUIRED output schema.

Your job:
- Output ONLY ONE JSON object (no markdown, no explanations)
- It MUST match the required output schema in the message
- Produce the NEXT step only

PLANNER_MESSAGE_JSON:
%s

YOUR OUTPUT (JSON only):
""".formatted(plannerMessageJson);
    }
}
