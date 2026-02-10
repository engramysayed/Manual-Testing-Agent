package aiLayer;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.PropertyReader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LLMClient {

    private final HttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public LLMClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = PropertyReader.getProperty("ANTHROPIC_API_KEY");
        this.apiUrl = "https://api.anthropic.com/v1/messages";
        this.model = "claude-sonnet-4-20250514";
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ANTHROPIC_API_KEY is missing.");
        }
    }

    public String call(String prompt, int maxTokens) throws Exception {

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("max_tokens", maxTokens);

        JSONArray messages = new JSONArray();
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", prompt);
        messages.put(user);

        body.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Anthropic API error: " + response.statusCode() + " -> " + response.body());
        }

        return extractText(response.body());
    }

    private String extractText(String apiResponseJson) {

        JSONObject root = new JSONObject(apiResponseJson);
        JSONArray content = root.optJSONArray("content");

        if (content == null || content.isEmpty()) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            JSONObject item = content.optJSONObject(i);
            if (item == null) continue;

            String type = item.optString("type", "");
            if ("text".equalsIgnoreCase(type)) {
                out.append(item.optString("text", ""));
            }
        }

        return out.toString().trim();
    }
}
