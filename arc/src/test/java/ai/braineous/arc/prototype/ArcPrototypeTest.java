package ai.braineous.arc.prototype;

import org.junit.jupiter.api.Test;

import ai.braineous.arc.IntelligenceBridge;

class ArcPrototypeTest {

    @Test
    void test_1() {
        String requestJson = "{\"prompt\":\"You are an execution engine, not a document reader.\\nReturn only JSON.\\nUse the provided output_template as the final answer format.\\nExecute this prompt using only the provided context and task.\\nDo not describe, summarize, explain, or analyze this request.\\nUse context.nodes as the system state.\\nUse task.factId as the primary fact.\\nUse each task.relatedFactId as a system fact related directly to the primary fact identified by task.factId.\\nDo not infer relationships between related facts unless explicitly provided by context.\\nUse task.controls only to understand the intent of the task.\\nDo not treat task.controls as additional facts.\\nDo not infer missing facts from task.controls.\\nDo not recompute task.controls from context.\\nReturn compact JSON on a single line.\\nDo not include spaces, tabs, or newlines outside JSON syntax.\\nSet every value in output_template as a string.\\nReturn exactly the output_template shape.\\nDo not add, remove, or rename any fields.\\nReturn exactly one JSON object.\\nDo not wrap the JSON in markdown fences.\\nDo not include explanation before or after the JSON.\\nSet result.decision as a string value derived from llm_query execution.\\nSet result.reason as a string value derived from llm_query execution.\\nSet result.code as a string value derived from llm_query execution.\\n\\nINPUT:\\n{\\\"task\\\":{\\\"intent\\\":{\\\"goal\\\":\\\"decision\\\"},\\\"factId\\\":\\\"PaymentRequest:PAY-1001\\\",\\\"relatedFactIds\\\":[\\\"CustomerAccount:CUST-2001\\\",\\\"PaymentMethod:PM-3001\\\",\\\"RiskProfile:RISK-4001\\\",\\\"MerchantPolicy:POL-5001\\\"],\\\"select\\\":[\\\"decision\\\",\\\"reason\\\",\\\"code\\\"],\\\"controls\\\":{\\\"intent\\\":\\\"decide_payment_capture\\\",\\\"action\\\":\\\"determine\\\",\\\"subject\\\":\\\"primary_payment_request\\\",\\\"decision\\\":\\\"allow_capture\\\",\\\"basis\\\":\\\"related_system_facts\\\",\\\"goal\\\":\\\"decision\\\"}},\\\"context\\\":{\\\"nodes\\\":{\\\"CustomerAccount:CUST-2001\\\":{\\\"id\\\":\\\"CustomerAccount:CUST-2001\\\",\\\"text\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"CustomerAccount:CUST-2001\\\\\\\",\\\\\\\"kind\\\\\\\":\\\\\\\"CustomerAccount\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"atomic\\\\\\\",\\\\\\\"status\\\\\\\":\\\\\\\"ACTIVE\\\\\\\"}\\\",\\\"attributes\\\":[],\\\"mode\\\":\\\"ATOMIC\\\"},\\\"RiskProfile:RISK-4001\\\":{\\\"id\\\":\\\"RiskProfile:RISK-4001\\\",\\\"text\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"RiskProfile:RISK-4001\\\\\\\",\\\\\\\"kind\\\\\\\":\\\\\\\"RiskProfile\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"atomic\\\\\\\",\\\\\\\"level\\\\\\\":\\\\\\\"LOW\\\\\\\"}\\\",\\\"attributes\\\":[],\\\"mode\\\":\\\"ATOMIC\\\"},\\\"PaymentRequest:PAY-1001\\\":{\\\"id\\\":\\\"PaymentRequest:PAY-1001\\\",\\\"text\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"PaymentRequest:PAY-1001\\\\\\\",\\\\\\\"kind\\\\\\\":\\\\\\\"PaymentRequest\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"atomic\\\\\\\",\\\\\\\"amount\\\\\\\":\\\\\\\"125.00\\\\\\\",\\\\\\\"currency\\\\\\\":\\\\\\\"USD\\\\\\\"}\\\",\\\"attributes\\\":[],\\\"mode\\\":\\\"ATOMIC\\\"},\\\"PaymentMethod:PM-3001\\\":{\\\"id\\\":\\\"PaymentMethod:PM-3001\\\",\\\"text\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"PaymentMethod:PM-3001\\\\\\\",\\\\\\\"kind\\\\\\\":\\\\\\\"PaymentMethod\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"atomic\\\\\\\",\\\\\\\"type\\\\\\\":\\\\\\\"CARD\\\\\\\"}\\\",\\\"attributes\\\":[],\\\"mode\\\":\\\"ATOMIC\\\"},\\\"MerchantPolicy:POL-5001\\\":{\\\"id\\\":\\\"MerchantPolicy:POL-5001\\\",\\\"text\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"MerchantPolicy:POL-5001\\\\\\\",\\\\\\\"kind\\\\\\\":\\\\\\\"MerchantPolicy\\\\\\\",\\\\\\\"mode\\\\\\\":\\\\\\\"atomic\\\\\\\",\\\\\\\"capture\\\\\\\":\\\\\\\"AUTO\\\\\\\"}\\\",\\\"attributes\\\":[],\\\"mode\\\":\\\"ATOMIC\\\"}}},\\\"output_template\\\":{\\\"result\\\":{\\\"decision\\\":\\\"\\\",\\\"reason\\\":\\\"\\\",\\\"code\\\":\\\"\\\"}}}\",\"stream\":false}";
        String environmentJson = "{}";
        int iterations = 50;
        IntelligenceBridge intelligenceBridge = new IntelligenceBridge();

        for (int i = 1; i <= iterations; i++) {
            System.out.println("============================================================");
            System.out.println("ITERATION " + i);
            System.out.println("============================================================");
            System.out.println();
            System.out.println("A1 — Exact Arc input");
            System.out.print(requestJson);
            System.out.print("\n");
            System.out.println();

            try {
                String response = intelligenceBridge.invoke(requestJson, environmentJson);
                System.out.println("ARC COMPONENT RESPONSE");
                System.out.print(response);
                System.out.print("\n");
            } catch (Exception exception) {
                System.out.println("ARC COMPONENT EXCEPTION");
                System.out.println(exception.getClass().getName());
                System.out.println(exception.getMessage());
            }
        }
    }
}
