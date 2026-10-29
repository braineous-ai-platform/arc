package ai.braineous.arc;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

class V14JitPromptIT {

    @Test
    void test_1() throws Exception {
        try {
            InputStream stream =
                    V14JitPromptIT.class.getResourceAsStream(
                            "/ai/braineous/arc/v14-jit-query.json");
            if (stream == null) {
                throw new RuntimeException(
                        "missing resource /ai/braineous/arc/v14-jit-query.json");
            }

            byte[] bytes;
            try {
                bytes = stream.readAllBytes();
            } finally {
                stream.close();
            }

            String queryJson = new String(bytes, StandardCharsets.UTF_8);

            System.out.print("=== JIT QUERY RESOURCE BEGIN ===\n");
            System.out.print(queryJson);
            System.out.print("\n=== JIT QUERY RESOURCE END ===\n");

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
        int runCount = Integer.parseInt(System.getProperty("v14.jit.runs", "5"));

        InputStream stream =
                V14JitPromptIT.class.getResourceAsStream(
                        "/ai/braineous/arc/v14-jit-query.json");
        if (stream == null) {
            throw new RuntimeException(
                    "missing resource /ai/braineous/arc/v14-jit-query.json");
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
        Map<String, Integer> frequencies = new LinkedHashMap<String, Integer>();
        int completed = 0;

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

                if (response != null) {
                    Integer current = frequencies.get(response);
                    if (current == null) {
                        frequencies.put(response, Integer.valueOf(1));
                    } else {
                        frequencies.put(response, Integer.valueOf(current.intValue() + 1));
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            completed = completed + 1;
            System.out.print("=== RUN " + run + " END ===\n");
        }

        System.out.print("=== OBSERVATION SUMMARY BEGIN ===\n");
        System.out.print("completed invocations: " + completed + "\n");
        System.out.print("unique raw responses: " + frequencies.size() + "\n");
        System.out.print("=== RAW RESPONSE FREQUENCY BEGIN ===\n");
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            System.out.print("count=" + entry.getValue() + "\n");
            System.out.print("--- RAW BEGIN ---\n");
            System.out.print(entry.getKey());
            System.out.print("\n--- RAW END ---\n");
        }
        System.out.print("=== RAW RESPONSE FREQUENCY END ===\n");
        System.out.print("=== OBSERVATION SUMMARY END ===\n");
    }
}
