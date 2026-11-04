package ai.braineous.arc;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import ai.braineous.arc.prototype.ArcPrototypeTest;

class JitReplenishIT {

    @Test
    void test_1() throws Exception {
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

        int attempted = 0;
        int returned = 0;
        int exceptions = 0;
        int success = 0;
        int contamination = 0;
        int noise = 0;

        long wallStartMs = System.currentTimeMillis();
        attempted = attempted + 1;
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

            returned = returned + 1;

            boolean hasFail = false;
            boolean hasSuccess = false;
            if (response != null) {
                hasFail = response.contains("fail");
                hasSuccess = response.contains("success");
            }
            if (hasSuccess && !hasFail) {
                success = success + 1;
            } else if (hasFail && !hasSuccess) {
                contamination = contamination + 1;
            } else {
                noise = noise + 1;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            exceptions = exceptions + 1;
        }
        long wallMs = System.currentTimeMillis() - wallStartMs;

        String successPct = "0";
        String noisePct = "0";
        String contaminationPct = "0";
        if (returned > 0) {
            successPct = Integer.toString((success * 100) / returned);
            noisePct = Integer.toString((noise * 100) / returned);
            contaminationPct = Integer.toString((contamination * 100) / returned);
        }

        System.out.print("=== OBSERVATION SUMMARY BEGIN ===\n");
        System.out.print("attempted invocations: " + attempted + "\n");
        System.out.print("returned invocations: " + returned + "\n");
        System.out.print("exceptions: " + exceptions + "\n");
        System.out.print("success: " + success + " (" + successPct + "%)\n");
        System.out.print("noise: " + noise + " (" + noisePct + "%)\n");
        System.out.print("contamination: " + contamination + " (" + contaminationPct + "%)\n");
        System.out.print("total wall-clock ms: " + wallMs + "\n");
        System.out.print("=== OBSERVATION SUMMARY END ===\n");
        System.out.print("=== OBSERVATION CHART DATA BEGIN ===\n");
        System.out.print("success: " + success + "\n");
        System.out.print("noise: " + noise + "\n");
        System.out.print("contamination: " + contamination + "\n");
        System.out.print("total: " + returned + "\n");
        System.out.print("=== OBSERVATION CHART DATA END ===\n");
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

        int attempted = 0;
        int returned = 0;
        int exceptions = 0;
        int success = 0;
        int contamination = 0;
        int noise = 0;

        long wallStartMs = System.currentTimeMillis();
        for (int run = 1; run <= runCount; run++) {
            attempted = attempted + 1;
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

                returned = returned + 1;

                boolean hasFail = false;
                boolean hasSuccess = false;
                if (response != null) {
                    hasFail = response.contains("fail");
                    hasSuccess = response.contains("success");
                }
                if (hasSuccess && !hasFail) {
                    success = success + 1;
                } else if (hasFail && !hasSuccess) {
                    contamination = contamination + 1;
                } else {
                    noise = noise + 1;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                exceptions = exceptions + 1;
            }
            System.out.print("=== RUN " + run + " END ===\n");
        }
        long wallMs = System.currentTimeMillis() - wallStartMs;

        String successPct = "0";
        String noisePct = "0";
        String contaminationPct = "0";
        if (returned > 0) {
            successPct = Integer.toString((success * 100) / returned);
            noisePct = Integer.toString((noise * 100) / returned);
            contaminationPct = Integer.toString((contamination * 100) / returned);
        }

        System.out.print("=== OBSERVATION SUMMARY BEGIN ===\n");
        System.out.print("attempted invocations: " + attempted + "\n");
        System.out.print("returned invocations: " + returned + "\n");
        System.out.print("exceptions: " + exceptions + "\n");
        System.out.print("success: " + success + " (" + successPct + "%)\n");
        System.out.print("noise: " + noise + " (" + noisePct + "%)\n");
        System.out.print("contamination: " + contamination + " (" + contaminationPct + "%)\n");
        System.out.print("total wall-clock ms: " + wallMs + "\n");
        System.out.print("=== OBSERVATION SUMMARY END ===\n");
        System.out.print("=== OBSERVATION CHART DATA BEGIN ===\n");
        System.out.print("success: " + success + "\n");
        System.out.print("noise: " + noise + "\n");
        System.out.print("contamination: " + contamination + "\n");
        System.out.print("total: " + returned + "\n");
        System.out.print("=== OBSERVATION CHART DATA END ===\n");
    }

    @Test
    void test_3() throws Exception {
        InputStream stream =
                JitReplenishIT.class.getResourceAsStream(
                        "/ai/braineous/arc/jit-review.json");
        if (stream == null) {
            throw new RuntimeException(
                    "missing resource /ai/braineous/arc/jit-review.json");
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

        int attempted = 0;
        int returned = 0;
        int exceptions = 0;
        int success = 0;
        int contamination = 0;
        int noise = 0;

        long wallStartMs = System.currentTimeMillis();
        attempted = attempted + 1;
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

            returned = returned + 1;

            boolean hasFail = false;
            boolean hasSuccess = false;
            if (response != null) {
                hasFail = response.contains("fail");
                hasSuccess = response.contains("success");
            }
            if (hasFail && !hasSuccess) {
                success = success + 1;
            } else if (hasSuccess && !hasFail) {
                contamination = contamination + 1;
            } else {
                noise = noise + 1;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            exceptions = exceptions + 1;
        }
        long wallMs = System.currentTimeMillis() - wallStartMs;

        String successPct = "0";
        String noisePct = "0";
        String contaminationPct = "0";
        if (returned > 0) {
            successPct = Integer.toString((success * 100) / returned);
            noisePct = Integer.toString((noise * 100) / returned);
            contaminationPct = Integer.toString((contamination * 100) / returned);
        }

        System.out.print("=== OBSERVATION SUMMARY BEGIN ===\n");
        System.out.print("attempted invocations: " + attempted + "\n");
        System.out.print("returned invocations: " + returned + "\n");
        System.out.print("exceptions: " + exceptions + "\n");
        System.out.print("success: " + success + " (" + successPct + "%)\n");
        System.out.print("noise: " + noise + " (" + noisePct + "%)\n");
        System.out.print("contamination: " + contamination + " (" + contaminationPct + "%)\n");
        System.out.print("total wall-clock ms: " + wallMs + "\n");
        System.out.print("=== OBSERVATION SUMMARY END ===\n");
        System.out.print("=== OBSERVATION CHART DATA BEGIN ===\n");
        System.out.print("success: " + success + "\n");
        System.out.print("noise: " + noise + "\n");
        System.out.print("contamination: " + contamination + "\n");
        System.out.print("total: " + returned + "\n");
        System.out.print("=== OBSERVATION CHART DATA END ===\n");
    }

    @Test
    void test_4() throws Exception {
        int runCount = Integer.parseInt(
                System.getProperty("jit.replenish.runs", "1"));

        InputStream stream =
                JitReplenishIT.class.getResourceAsStream(
                        "/ai/braineous/arc/jit-review.json");
        if (stream == null) {
            throw new RuntimeException(
                    "missing resource /ai/braineous/arc/jit-review.json");
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

        int attempted = 0;
        int returned = 0;
        int exceptions = 0;
        int success = 0;
        int contamination = 0;
        int noise = 0;

        long wallStartMs = System.currentTimeMillis();
        for (int run = 1; run <= runCount; run++) {
            attempted = attempted + 1;
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

                returned = returned + 1;

                boolean hasFail = false;
                boolean hasSuccess = false;
                if (response != null) {
                    hasFail = response.contains("fail");
                    hasSuccess = response.contains("success");
                }
                if (hasFail && !hasSuccess) {
                    success = success + 1;
                } else if (hasSuccess && !hasFail) {
                    contamination = contamination + 1;
                } else {
                    noise = noise + 1;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                exceptions = exceptions + 1;
            }
            System.out.print("=== RUN " + run + " END ===\n");
        }
        long wallMs = System.currentTimeMillis() - wallStartMs;

        String successPct = "0";
        String noisePct = "0";
        String contaminationPct = "0";
        if (returned > 0) {
            successPct = Integer.toString((success * 100) / returned);
            noisePct = Integer.toString((noise * 100) / returned);
            contaminationPct = Integer.toString((contamination * 100) / returned);
        }

        System.out.print("=== OBSERVATION SUMMARY BEGIN ===\n");
        System.out.print("attempted invocations: " + attempted + "\n");
        System.out.print("returned invocations: " + returned + "\n");
        System.out.print("exceptions: " + exceptions + "\n");
        System.out.print("success: " + success + " (" + successPct + "%)\n");
        System.out.print("noise: " + noise + " (" + noisePct + "%)\n");
        System.out.print("contamination: " + contamination + " (" + contaminationPct + "%)\n");
        System.out.print("total wall-clock ms: " + wallMs + "\n");
        System.out.print("=== OBSERVATION SUMMARY END ===\n");
        System.out.print("=== OBSERVATION CHART DATA BEGIN ===\n");
        System.out.print("success: " + success + "\n");
        System.out.print("noise: " + noise + "\n");
        System.out.print("contamination: " + contamination + "\n");
        System.out.print("total: " + returned + "\n");
        System.out.print("=== OBSERVATION CHART DATA END ===\n");
    }
}
