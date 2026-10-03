package ai.braineous.arc.prototype;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ai.braineous.arc.IntelligenceBridge;

class ArcPrototypeTest {

    @Test
    void test_1() throws Exception {
        String requestJson = "{\"prompt\":\"You are an execution engine, not a document reader.\\nReturn only JSON.\\nUse the provided output_template as the final answer format.\\nExecute this prompt using only the provided context and task.\\nDo not describe, summarize, explain, or analyze this request.\\nUse context.nodes as the system state.\\nUse task.factId as the primary fact.\\nUse each task.relatedFactId as a system fact related directly to the primary fact identified by task.factId.\\nDo not infer relationships between related facts unless explicitly provided by context.\\nUse task.controls only to understand the intent of the task.\\nDo not treat task.controls as additional facts.\\nDo not infer missing facts from task.controls.\\nDo not recompute task.controls from context.\\nReturn compact JSON on a single line.\\nDo not include spaces, tabs, or newlines outside JSON syntax.\\nReturn exactly the output_template shape.\\nDo not add, remove, or rename any fields.\\nReturn exactly one JSON object.\\nDo not wrap the JSON in markdown fences.\\nDo not include explanation before or after the JSON.\\nSet result.decision as a string value derived from llm_query execution.\\nSet result.reason as a string value derived from llm_query execution.\\nSet result.code as a string value derived from llm_query execution.\\n\\nINPUT:\\n{\\\"task\\\":{\\\"intent\\\":{\\\"goal\\\":\\\"decision\\\"},\\\"factId\\\":\\\"PaymentRequest:PAY-1001\\\",\\\"relatedFactIds\\\":[\\\"CustomerAccount:CUST-2001\\\",\\\"PaymentMethod:PM-3001\\\",\\\"RiskProfile:RISK-4001\\\",\\\"MerchantPolicy:POL-5001\\\"],\\\"select\\\":[\\\"decision\\\",\\\"reason\\\",\\\"code\\\"],\\\"controls\\\":{\\\"intent\\\":\\\"decide_payment_capture\\\",\\\"action\\\":\\\"determine\\\",\\\"subject\\\":\\\"primary_payment_request\\\",\\\"decision\\\":\\\"allow_capture\\\",\\\"basis\\\":\\\"related_system_facts\\\",\\\"goal\\\":\\\"decision\\\"}},\\\"context\\\":{\\\"nodes\\\":{\\\"CustomerAccount:CUST-2001\\\":{\\\"id\\\":\\\"CustomerAccount:CUST-2001\\\",\\\"text\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"CustomerAccount:CUST-2001\\\\\\\",\\\\\\\"kind\\\\\\\":\\\\\\\"CustomerAccount\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"atomic\\\\\\\",\\\\\\\"status\\\\\\\":\\\\\\\"ACTIVE\\\\\\\"}\\\",\\\"attributes\\\":[],\\\"mode\\\":\\\"ATOMIC\\\"},\\\"RiskProfile:RISK-4001\\\":{\\\"id\\\":\\\"RiskProfile:RISK-4001\\\",\\\"text\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"RiskProfile:RISK-4001\\\\\\\",\\\\\\\"kind\\\\\\\":\\\\\\\"RiskProfile\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"atomic\\\\\\\",\\\\\\\"level\\\\\\\":\\\\\\\"LOW\\\\\\\"}\\\",\\\"attributes\\\":[],\\\"mode\\\":\\\"ATOMIC\\\"},\\\"PaymentRequest:PAY-1001\\\":{\\\"id\\\":\\\"PaymentRequest:PAY-1001\\\",\\\"text\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"PaymentRequest:PAY-1001\\\\\\\",\\\\\\\"kind\\\\\\\":\\\\\\\"PaymentRequest\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"atomic\\\\\\\",\\\\\\\"amount\\\\\\\":\\\\\\\"125.00\\\\\\\",\\\\\\\"currency\\\\\\\":\\\\\\\"USD\\\\\\\"}\\\",\\\"attributes\\\":[],\\\"mode\\\":\\\"ATOMIC\\\"},\\\"PaymentMethod:PM-3001\\\":{\\\"id\\\":\\\"PaymentMethod:PM-3001\\\",\\\"text\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"PaymentMethod:PM-3001\\\\\\\",\\\\\\\"kind\\\\\\\":\\\\\\\"PaymentMethod\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"atomic\\\\\\\",\\\\\\\"type\\\\\\\":\\\\\\\"CARD\\\\\\\"}\\\",\\\"attributes\\\":[],\\\"mode\\\":\\\"ATOMIC\\\"},\\\"MerchantPolicy:POL-5001\\\":{\\\"id\\\":\\\"MerchantPolicy:POL-5001\\\",\\\"text\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"MerchantPolicy:POL-5001\\\\\\\",\\\\\\\"kind\\\\\\\":\\\\\\\"MerchantPolicy\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"atomic\\\\\\\",\\\\\\\"capture\\\\\\\":\\\\\\\"AUTO\\\\\\\"}\\\",\\\"attributes\\\":[],\\\"mode\\\":\\\"ATOMIC\\\"}}},\\\"output_template\\\":{\\\"result\\\":{\\\"decision\\\":\\\"\\\",\\\"reason\\\":\\\"\\\",\\\"code\\\":\\\"\\\"}}}\",\"stream\":false}";
        String environmentJson = "{}";
        int iterations = 5;
        IntelligenceBridge intelligenceBridge = new IntelligenceBridge();

        ExecutorService executor = Executors.newFixedThreadPool(iterations);
        List<Future<?>> futures = new ArrayList<Future<?>>();
        long totalStartNanos = System.nanoTime();

        for (int i = 1; i <= iterations; i++) {
            final int iteration = i;
            futures.add(executor.submit(() -> {
                String response = null;
                try {
                    response = intelligenceBridge.invoke(requestJson, environmentJson);
                } catch (Exception exception) {
                    System.out.println("FAILING ITERATION " + iteration);
                    System.out.println("exception class: " + exception.getClass().getName());
                    System.out.println("exception message: " + exception.getMessage());
                    System.out.flush();
                    executor.shutdownNow();
                    throw new RuntimeException(exception);
                }

                String decision = extractField(response, "decision");
                String code = extractField(response, "code");
                System.out.println("ITERATION " + iteration + " decision=" + decision + " code=" + code);
                System.out.flush();

                if (!"allow_capture".equals(decision)) {
                    System.out.println("FAILING ITERATION " + iteration);
                    System.out.println("decision: " + decision);
                    System.out.println("code: " + code);
                    System.out.println("ARC COMPONENT RESPONSE");
                    System.out.print(response);
                    System.out.print("\n");
                    System.out.flush();
                    executor.shutdownNow();
                    throw new RuntimeException("invariant failed at iteration " + iteration);
                }
            }));
        }

        try {
            for (int i = 0; i < futures.size(); i++) {
                futures.get(i).get();
            }
        } catch (Exception exception) {
            executor.shutdownNow();
            Assertions.fail(exception.getMessage());
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        long elapsedTotalMs = (System.nanoTime() - totalStartNanos) / 1000000L;
        System.out.println("completed: " + iterations + "/" + iterations);
        System.out.println("decision=allow_capture: " + iterations + "/" + iterations);
        System.out.println("elapsed total ms: " + elapsedTotalMs);
        System.out.flush();
    }

    static String extractField(String response, String field) {
        if (response == null) {
            return null;
        }
        String marker = "\"" + field + "\"";
        int from = 0;
        while (from < response.length()) {
            int idx = response.indexOf(marker, from);
            if (idx < 0) {
                return null;
            }
            int colon = response.indexOf(':', idx + marker.length());
            if (colon < 0) {
                return null;
            }
            int quoteStart = -1;
            int cursor = colon + 1;
            while (cursor < response.length()) {
                char current = response.charAt(cursor);
                if (current == '"') {
                    quoteStart = cursor;
                    break;
                }
                if (!Character.isWhitespace(current)) {
                    break;
                }
                cursor++;
            }
            if (quoteStart >= 0) {
                int quoteEnd = response.indexOf('"', quoteStart + 1);
                if (quoteEnd > quoteStart) {
                    return response.substring(quoteStart + 1, quoteEnd);
                }
            }
            from = idx + marker.length();
        }
        return null;
    }
}
