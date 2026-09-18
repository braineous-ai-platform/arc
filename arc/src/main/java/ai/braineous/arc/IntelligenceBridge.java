package ai.braineous.arc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IntelligenceBridge {

    public String invoke(
            String requestJson,
            String environmentJson) {

        JsonElement requestElement = parseJson(requestJson, "requestJson");
        parseJson(environmentJson, "environmentJson");

        JsonObject arcRequest = requireObject(requestElement, "requestJson");
        String model = requireString(arcRequest, "model", "requestJson.model");
        String prompt = requireString(arcRequest, "prompt", "requestJson.prompt");
        boolean stream = requireBoolean(arcRequest, "stream", "requestJson.stream");

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject liteLLMRequest = new JsonObject();
        liteLLMRequest.addProperty("model", model);
        liteLLMRequest.add("messages", messages);
        liteLLMRequest.addProperty("stream", stream);

        String liteLLMRequestJson = liteLLMRequest.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4000/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(liteLLMRequestJson))
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

        JsonElement responseElement = parseJson(responseJson, "responseJson");
        JsonObject responseObject = requireObject(responseElement, "responseJson");

        if (!responseObject.has("choices")) {
            throw new RuntimeException("responseJson.choices is required");
        }

        JsonElement choicesElement = responseObject.get("choices");
        if (!choicesElement.isJsonArray()) {
            throw new RuntimeException("responseJson.choices must be an array");
        }

        JsonArray choices = choicesElement.getAsJsonArray();
        if (choices.isEmpty()) {
            throw new RuntimeException("responseJson.choices must not be empty");
        }

        JsonElement firstChoiceElement = choices.get(0);
        if (!firstChoiceElement.isJsonObject()) {
            throw new RuntimeException("responseJson.choices[0] must be an object");
        }

        JsonObject firstChoice = firstChoiceElement.getAsJsonObject();
        if (!firstChoice.has("message")) {
            throw new RuntimeException("responseJson.choices[0].message is required");
        }

        JsonElement messageElement = firstChoice.get("message");
        if (!messageElement.isJsonObject()) {
            throw new RuntimeException("responseJson.choices[0].message must be an object");
        }

        JsonObject responseMessage = messageElement.getAsJsonObject();
        if (!responseMessage.has("content")) {
            throw new RuntimeException("responseJson.choices[0].message.content is required");
        }

        JsonElement contentElement = responseMessage.get("content");
        if (!contentElement.isJsonPrimitive()
                || !contentElement.getAsJsonPrimitive().isString()) {
            throw new RuntimeException(
                    "responseJson.choices[0].message.content must be a string");
        }

        String content = contentElement.getAsString();

        return content;
    }

    HttpResponse<String> send(HttpRequest request)
            throws IOException, InterruptedException {

        HttpClient client =
                HttpClient.newHttpClient();

        return client.send(
                request,
                HttpResponse.BodyHandlers.ofString());
    }

    private JsonElement parseJson(String json, String name) {
        if (json == null) {
            throw new RuntimeException(name + " must not be null");
        }

        if (json.isBlank()) {
            throw new RuntimeException(name + " must not be blank");
        }

        try {
            return JsonParser.parseString(json);
        } catch (JsonParseException exception) {
            throw new RuntimeException(name + " must contain valid JSON", exception);
        }
    }

    private JsonObject requireObject(JsonElement element, String name) {
        if (!element.isJsonObject()) {
            throw new RuntimeException(name + " must be a JSON object");
        }

        return element.getAsJsonObject();
    }

    private String requireString(JsonObject object, String field, String name) {
        if (!object.has(field)) {
            throw new RuntimeException(name + " is required");
        }

        JsonElement element = object.get(field);
        if (!element.isJsonPrimitive()) {
            throw new RuntimeException(name + " must be a string");
        }

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isString()) {
            throw new RuntimeException(name + " must be a string");
        }

        return primitive.getAsString();
    }

    private boolean requireBoolean(JsonObject object, String field, String name) {
        if (!object.has(field)) {
            throw new RuntimeException(name + " is required");
        }

        JsonElement element = object.get(field);
        if (!element.isJsonPrimitive()) {
            throw new RuntimeException(name + " must be a boolean");
        }

        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (!primitive.isBoolean()) {
            throw new RuntimeException(name + " must be a boolean");
        }

        return primitive.getAsBoolean();
    }
}
