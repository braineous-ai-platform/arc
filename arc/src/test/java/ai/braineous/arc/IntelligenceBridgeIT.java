package ai.braineous.arc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class IntelligenceBridgeIT {

    @Test
    void test_1() {
        String requestJson = "{\"model\":\"qwen2.5:0.5b\",\"messages\":[{\"role\":\"user\",\"content\":\"Reply with exactly: ARC_OK\"}],\"temperature\":0,\"seed\":42,\"max_tokens\":16,\"stream\":false}";
        String environmentJson = "{}";

        IntelligenceBridge intelligenceBridge = new IntelligenceBridge();

        String responseJson =
                intelligenceBridge.invoke(
                        requestJson,
                        environmentJson);

        System.out.println("REQUEST:");
        System.out.println(requestJson);

        System.out.println("ENVIRONMENT:");
        System.out.println(environmentJson);

        System.out.println("RESPONSE:");
        System.out.println(responseJson);

        assertNotNull(responseJson);
        assertFalse(responseJson.isBlank());

        JsonObject response = JsonParser.parseString(responseJson).getAsJsonObject();

        assertEquals(
                "ARC_OK",
                response.getAsJsonArray("choices")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content")
                        .getAsString());
        assertEquals("qwen2.5:0.5b", response.get("model").getAsString());
    }
}
