package ai.braineous.arc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class JitReplenishIT {

    @Test
    void test_review_unavailable() throws Exception {
        int runCount = Integer.parseInt(
                System.getProperty("jit.replenish.runs", "1"));
        // int runCount = 20;

        InputStream stream = JitReplenishIT.class.getResourceAsStream(
                "/ai/braineous/arc/jit-review-unavailable.json");
        if (stream == null) {
            throw new RuntimeException(
                    "missing resource /ai/braineous/arc/jit-review-unavailable.json");
        }

        this.execute(stream, runCount);

    }

    @Test
    void test_review_available() throws Exception {
        int runCount = Integer.parseInt(
                System.getProperty("jit.replenish.runs", "1"));
        // int runCount = 20;

        InputStream stream = JitReplenishIT.class.getResourceAsStream(
                "/ai/braineous/arc/jit-review-available.json");
        if (stream == null) {
            throw new RuntimeException(
                    "missing resource /ai/braineous/arc/jit-review-available.json");
        }

        this.execute(stream, runCount);

    }

    @Test
    void test_security_account_investigate() throws Exception {
        int runCount = Integer.parseInt(
                System.getProperty("jit.replenish.runs", "1"));
        // int runCount = 20;

        InputStream stream = JitReplenishIT.class.getResourceAsStream(
                "/ai/braineous/arc/security-account-investigate.json");
        if (stream == null) {
            throw new RuntimeException(
                    "missing resource /ai/braineous/arc/security-account-investigate.json");
        }

        this.execute(stream, runCount);

    }

    @Test
    void test_security_account_routine() throws Exception {
        int runCount = Integer.parseInt(
                System.getProperty("jit.replenish.runs", "1"));
        // int runCount = 20;

        InputStream stream = JitReplenishIT.class.getResourceAsStream(
                "/ai/braineous/arc/security-account-routine.json");
        if (stream == null) {
            throw new RuntimeException(
                    "missing resource /ai/braineous/arc/security-account-routine.json");
        }

        this.execute(stream, runCount);

    }

    private void execute(InputStream stream, int runCount) {
        byte[] bytes;
        try {
            bytes = stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
                throw new RuntimeException();
            }
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

                String response = intelligenceBridge.invoke(
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

    private static final class Qwen15ComparisonBridge extends IntelligenceBridge {

        @Override
        HttpResponse<String> send(HttpRequest original)
                throws IOException, InterruptedException {

            String originalBody = readBody(original);
            JsonObject liteLLMRequest = JsonParser.parseString(originalBody).getAsJsonObject();
            liteLLMRequest.addProperty("model", "qwen2.5:0.5b");
            liteLLMRequest.addProperty("max_tokens", 64);

            HttpRequest rewritten = HttpRequest.newBuilder()
                    .uri(original.uri())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(liteLLMRequest.toString()))
                    .build();

            return HttpClient.newHttpClient().send(
                    rewritten,
                    HttpResponse.BodyHandlers.ofString());
        }

        private static String readBody(HttpRequest request) {
            Optional<HttpRequest.BodyPublisher> publisher = request.bodyPublisher();
            if (publisher.isEmpty()) {
                throw new RuntimeException("LiteLLM request body is required");
            }

            BodyCollector collector = new BodyCollector();
            publisher.get().subscribe(collector);
            if (collector.failure != null) {
                throw new RuntimeException(
                        "LiteLLM request body could not be read",
                        collector.failure);
            }
            return collector.body.toString(StandardCharsets.UTF_8);
        }
    }

    private static final class BodyCollector implements Flow.Subscriber<ByteBuffer> {

        private final ByteArrayOutputStream body = new ByteArrayOutputStream();
        private Throwable failure;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            byte[] bytes = new byte[item.remaining()];
            item.get(bytes);
            body.writeBytes(bytes);
        }

        @Override
        public void onError(Throwable throwable) {
            failure = throwable;
        }

        @Override
        public void onComplete() {
        }
    }
}
