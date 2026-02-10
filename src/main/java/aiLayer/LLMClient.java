package aiLayer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LLMClient {

    private final HttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;

    public LLMClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = System.getenv("ANTHROPIC_API_KEY");
        this.apiUrl = "https://api.anthropic.com/v1/messages";
    }

    public String callLLM(String prompt) throws Exception {
        String requestBody = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("LLM API error: " + response.body());
        }

        return extractContent(response.body());
    }

    private String buildRequestBody(String prompt) {
        return String.format("""
            {
              "model": "claude-sonnet-4-20250514",
              "max_tokens": 1000,
              "messages": [
                {
                  "role": "user",
                  "content": "%s"
                }
              ]
            }
            """, escapeJson(prompt));
    }

    private String extractContent(String apiResponse) {
        // Simple JSON extraction (use proper JSON parser in production)
        int contentStart = apiResponse.indexOf("\"text\":\"") + 8;
        int contentEnd = apiResponse.indexOf("\"", contentStart);
        return apiResponse.substring(contentStart, contentEnd)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}