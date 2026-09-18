package ai.braineous.arc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IntelligenceBridgeIT {

    @Test
    void test_1() {
        String requestJson = "{\"prompt\":\"Reply with exactly: ARC_OK\",\"stream\":false}";
        String environmentJson = "{}";

        IntelligenceBridge intelligenceBridge = new IntelligenceBridge();

        String response =
                intelligenceBridge.invoke(
                        requestJson,
                        environmentJson);

        System.out.println("REQUEST:");
        System.out.println(requestJson);

        System.out.println("ENVIRONMENT:");
        System.out.println(environmentJson);

        System.out.println("RESPONSE:");
        System.out.println(response);

        assertEquals("ARC_OK", response);
    }
}
