package ai.braineous.arc;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import ai.braineous.arc.prototype.ArcPrototypeTest;

class JitReplenishIT {

    @Test
    void test_1() throws Exception {
        try {
            InputStream stream =
                    JitReplenishIT.class.getResourceAsStream(
                            "/ai/braineous/arc/jit-replenish.json");
            if (stream == null) {
                throw new RuntimeException(
                        "missing resource /ai/braineous/arc/jit-replenish.json");
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

            String extracted = ArcPrototypeTest.extractResultValue(response);
            boolean accepted = false;
            if (extracted != null) {
                if (extracted.equals("READY")) {
                    accepted = true;
                }
            }
            if (!accepted) {
                throw new RuntimeException(
                        "V14 result assertion failed\n"
                                + "=== INTELLIGENCEBRIDGE RESPONSE BEGIN ===\n"
                                + response
                                + "\n=== INTELLIGENCEBRIDGE RESPONSE END ===\n");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            throw exception;
        }
    }

    @Test
    void test_2() throws Exception {
        int runCount = Integer.parseInt(
                System.getProperty("jit.replenish.runs", "1"));

        InputStream stream =
                JitReplenishIT.class.getResourceAsStream(
                        "/ai/braineous/arc/jit-replenish.json");
        if (stream == null) {
            throw new RuntimeException(
                    "missing resource /ai/braineous/arc/jit-replenish.json");
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

        int correct = 0;
        int incorrect = 0;
        int unresolved = 0;
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

                String extracted = ArcPrototypeTest.extractResultValue(response);
                if (extracted == null) {
                    unresolved = unresolved + 1;
                } else {
                    if (extracted.equals("READY")) {
                        correct = correct + 1;
                    } else {
                        incorrect = incorrect + 1;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                unresolved = unresolved + 1;
            }
            completed = completed + 1;
            System.out.print("=== RUN " + run + " END ===\n");
        }

        int failures = incorrect + unresolved;
        String failureRate = "0";
        if (completed > 0) {
            failureRate = Integer.toString((failures * 100) / completed);
        }

        System.out.print("=== OBSERVATION SUMMARY BEGIN ===\n");
        System.out.print("completed invocations: " + completed + "\n");
        System.out.print("correct READY: " + correct + "\n");
        System.out.print("incorrect decisions: " + incorrect + "\n");
        System.out.print("missing/unresolved: " + unresolved + "\n");
        System.out.print("semantic failures: " + failures + "\n");
        System.out.print("semantic failure rate: " + failureRate + "%\n");
        System.out.print("=== OBSERVATION SUMMARY END ===\n");
    }
}
