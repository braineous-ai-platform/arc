package ai.braineous.arc.prototype;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

class LiteLLMPrototypeTest {

    @Test
    void sendsChatCompletionThroughLiteLLM() throws IOException, InterruptedException {
        String requestJson = "{\"model\":\"qwen2.5:0.5b\",\"messages\":[{\"role\":\"user\",\"content\":\"Reply with exactly: ARC_OK\"}],\"temperature\":0,\"seed\":42,\"max_tokens\":16,\"stream\":false}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4000/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString());

        String responseJson = response.body();

        System.out.println("REQUEST:");
        System.out.println(requestJson);

        System.out.println("RESPONSE:");
        System.out.println(responseJson);

        assertEquals(200, response.statusCode());
        assertNotNull(responseJson);
        assertFalse(responseJson.isBlank());
    }
}
