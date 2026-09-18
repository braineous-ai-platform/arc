package ai.braineous.arc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IntelligenceBridge {

    public String invoke(
            String requestJson,
            String environmentJson) {

        validateJson(requestJson, "requestJson");
        validateJson(environmentJson, "environmentJson");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4000/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response;
        try {
            response = send(request);
        } catch (IOException exception) {
            throw new RuntimeException("LiteLLM invocation failed", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("LiteLLM invocation was interrupted", exception);
        }

        String responseJson = response.body();

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "LiteLLM invocation failed with HTTP status " + response.statusCode());
        }

        validateJson(responseJson, "responseJson");

        return responseJson;
    }

    HttpResponse<String> send(HttpRequest request)
            throws IOException, InterruptedException {

        HttpClient client =
                HttpClient.newHttpClient();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    private void validateJson(String json, String name) {
        if (json == null) {
            throw new RuntimeException(name + " must not be null");
        }

        if (json.isBlank()) {
            throw new RuntimeException(name + " must not be blank");
        }

        try {
            JsonParser.parseString(json);
        } catch (JsonParseException exception) {
            throw new RuntimeException(name + " must contain valid JSON", exception);
        }
    }
}
