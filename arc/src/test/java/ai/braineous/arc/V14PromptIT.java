package ai.braineous.arc;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

class V14PromptIT {

    @Test
    void test_1() throws Exception {
        try {
            InputStream stream =
                    V14PromptIT.class.getResourceAsStream(
                            "/ai/braineous/arc/v14-query.json");
            if (stream == null) {
                throw new RuntimeException(
                        "missing resource /ai/braineous/arc/v14-query.json");
            }

            byte[] bytes;
            try {
                bytes = stream.readAllBytes();
            } finally {
                stream.close();
            }

            String queryJson = new String(bytes, StandardCharsets.UTF_8);

            JsonObject request = new JsonObject();
            request.addProperty("prompt", queryJson);
            request.addProperty("stream", false);
            String requestJson = request.toString();
            String environmentJson = "{}";

            IntelligenceBridge intelligenceBridge = new IntelligenceBridge();

            System.out.print("=== INTELLIGENCEBRIDGE REQUEST BEGIN ===\n");
            System.out.print(requestJson);
            System.out.print("\n=== INTELLIGENCEBRIDGE REQUEST END ===\n");

            String response =
                    intelligenceBridge.invoke(
                            requestJson,
                            environmentJson);

            System.out.print("=== INTELLIGENCEBRIDGE RESPONSE BEGIN ===\n");
            System.out.print(response);
            System.out.print("\n=== INTELLIGENCEBRIDGE RESPONSE END ===\n");
        } catch (Exception exception) {
            exception.printStackTrace();
            throw exception;
        }
    }

    @Test
    void test_2() throws Exception {
        int runCount = Integer.parseInt(System.getProperty("v14.runs", "1"));

        InputStream stream =
                V14PromptIT.class.getResourceAsStream(
                        "/ai/braineous/arc/v14-query.json");
        if (stream == null) {
            throw new RuntimeException(
                    "missing resource /ai/braineous/arc/v14-query.json");
        }

        byte[] bytes;
        try {
            bytes = stream.readAllBytes();
        } finally {
            stream.close();
        }

        String queryJson = new String(bytes, StandardCharsets.UTF_8);

        JsonObject request = new JsonObject();
        request.addProperty("prompt", queryJson);
        request.addProperty("stream", false);
        String requestJson = request.toString();
        String environmentJson = "{}";

        IntelligenceBridge intelligenceBridge = new IntelligenceBridge();

        for (int run = 1; run <= runCount; run++) {
            System.out.print("=== RUN " + run + " BEGIN ===\n");
            try {
                System.out.print("=== INTELLIGENCEBRIDGE REQUEST BEGIN ===\n");
                System.out.print(requestJson);
                System.out.print("\n=== INTELLIGENCEBRIDGE REQUEST END ===\n");

                String response =
                        intelligenceBridge.invoke(
                                requestJson,
                                environmentJson);

                System.out.print("=== INTELLIGENCEBRIDGE RESPONSE BEGIN ===\n");
                System.out.print(response);
                System.out.print("\n=== INTELLIGENCEBRIDGE RESPONSE END ===\n");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            System.out.print("=== RUN " + run + " END ===\n");
        }
    }
}
