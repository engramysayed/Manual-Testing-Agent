package aiLayer;
import utils.FilesManager;

import java.nio.file.Path;

public class LLMPlanner {

    private final Path runFolder;
    private final LLMClient llmClient;

    public LLMPlanner(Path runFolder) {
        this.runFolder = runFolder;
        this.llmClient = new LLMClient();
        FilesManager.createDirectory(String.valueOf(runFolder.resolve("aiLayer")));
    }

    public String getNextStep(String stepId, String stateJson) throws Exception {
        // Build prompt
        String prompt = buildPrompt(stateJson);

        // Save prompt
        Path promptFile = runFolder.resolve("aiLayer")
                .resolve(String.format("step_%s_prompt.txt", stepId));
        FilesManager.writeFile(promptFile, prompt);

        // Call LLM
        String response = llmClient.callLLM(prompt);

        // Save response
        Path responseFile = runFolder.resolve("aiLayer")
                .resolve(String.format("step_%s_response.json", stepId));
        FilesManager.writeFile(responseFile, response);

        return response;
    }

    private String buildPrompt(String stateJson) {
        return """
            You are a test automation planner. Given the current browser state, output the NEXT SINGLE step as strict JSON.
            
            RULES:
            - Output ONLY valid JSON, no explanation
            - ONE step per response
            - Use the exact schema provided
            
            CURRENT STATE:
            %s
            
            OUTPUT FORMAT (strict JSON):
            {
              "step_id": <number>,
              "action": "click|type|select|wait|navigate|done",
              "target": {
                "ref": {
                  "type": "css|xpath|id",
                  "value": "<selector>"
                }
              },
              "input": "<text to type (for type action)>",
              "wait": { "after_ms": 1000 },
              "assertions": [],
              "notes": "optional explanation"
            }
            
            If the test is complete, return: {"action": "done"}
            
            YOUR RESPONSE (JSON only):
            """.formatted(stateJson);
    }
}