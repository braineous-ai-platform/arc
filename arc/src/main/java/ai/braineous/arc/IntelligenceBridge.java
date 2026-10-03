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

    private static final String MODEL = "qwen2.5:0.5b";

    public String invoke(
            String requestJson,
            String environmentJson) {

        JsonElement requestElement = parseJson(requestJson, "requestJson");
        parseJson(environmentJson, "environmentJson");

        JsonObject arcRequest = requireObject(requestElement, "requestJson");
        String prompt = requireString(arcRequest, "prompt", "requestJson.prompt");
        boolean stream = requireBoolean(arcRequest, "stream", "requestJson.stream");

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject liteLLMRequest = new JsonObject();
        liteLLMRequest.addProperty("model", MODEL);
        liteLLMRequest.addProperty("max_tokens", 256);
        liteLLMRequest.add("messages", messages);

        String liteLLMRequestJson = liteLLMRequest.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4000/v1/messages"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(liteLLMRequestJson))
                .build();

        System.out.println("____arc.outbound.method____");
        System.out.println(request.method());
        System.out.println("____arc.outbound.uri____");
        System.out.println(request.uri().toString());
        System.out.println("____arc.outbound.headers____");
        request.headers().map().forEach((name, values) -> {
            for (String value : values) {
                System.out.println(name + ": " + value);
            }
        });
        System.out.println("____arc.outbound.timeout____");
        System.out.println(request.timeout());
        System.out.println("____arc.outbound.version____");
        System.out.println(request.version());
        System.out.println("____arc.outbound.expectContinue____");
        System.out.println(request.expectContinue());
        System.out.println("____arc.outbound.body____");
        System.out.print(liteLLMRequestJson);
        System.out.print("\n");
        System.out.println("____arc.outbound.body.end____");
        System.out.println("____arc.prototype.artifact2____");
        System.out.print(liteLLMRequestJson);
        System.out.print("\n");
        try {
            java.nio.file.Files.write(
                    java.nio.file.Path.of("/tmp/artifact2-body.txt"),
                    liteLLMRequestJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }

        HttpResponse<String> response;
        try {
            response = send(request);
            System.out.println("____arc.prototype.artifact2.response____");
            System.out.print(response.body());
            System.out.print("\n");
            System.out.println("____arc.outbound.status____");
            System.out.println(response.statusCode());
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

        if (!responseObject.has("content")) {
            throw new RuntimeException("responseJson.content is required");
        }

        JsonElement contentElement = responseObject.get("content");
        if (!contentElement.isJsonArray()) {
            throw new RuntimeException("responseJson.content must be an array");
        }

        JsonArray content = contentElement.getAsJsonArray();
        if (content.isEmpty()) {
            throw new RuntimeException("responseJson.content must not be empty");
        }

        String generatedText = null;
        for (JsonElement partElement : content) {
            if (!partElement.isJsonObject()) {
                continue;
            }

            JsonObject part = partElement.getAsJsonObject();
            if (!part.has("type")
                    || !part.get("type").isJsonPrimitive()
                    || !"text".equals(part.get("type").getAsString())) {
                continue;
            }

            if (!part.has("text")
                    || !part.get("text").isJsonPrimitive()
                    || !part.get("text").getAsJsonPrimitive().isString()) {
                throw new RuntimeException(
                        "responseJson.content generated text must be a string");
            }

            generatedText = part.get("text").getAsString();
            break;
        }

        if (generatedText == null) {
            throw new RuntimeException("responseJson.content generated text is required");
        }

        return generatedText;
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
