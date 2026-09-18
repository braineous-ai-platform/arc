package ai.braineous.arc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.function.BiPredicate;

import javax.net.ssl.SSLSession;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonParseException;

class IntelligenceBridgeTest {

    private static final String REQUEST_JSON =
            "{\"model\":\"qwen2.5:0.5b\",\"prompt\":\"Reply with exactly: ARC_OK\",\"stream\":false}";
    private static final String ENVIRONMENT_JSON = "{}";
    private static final String RESPONSE_JSON =
            "{\"id\":\"controlled-response\",\"choices\":[{\"message\":{\"content\":\"ARC_OK\"}}]}";
    private static final String LITELLM_REQUEST_JSON =
            "{\"model\":\"qwen2.5:0.5b\",\"messages\":[{\"role\":\"user\",\"content\":\"Reply with exactly: ARC_OK\"}],\"stream\":false}";

    @Test
    void test_1() {
        TestIntelligenceBridge bridge = successfulBridge();

        String response = bridge.invoke(REQUEST_JSON, ENVIRONMENT_JSON);

        assertEquals("ARC_OK", response);
    }

    @Test
    void test_2() {
        TestIntelligenceBridge bridge = new TestIntelligenceBridge();
        bridge.statusCode = 200;
        bridge.responseBody =
                "{\"choices\":[{\"message\":{\"content\":\"  line one\\nline two  \"}}]}";

        String response = bridge.invoke(REQUEST_JSON, ENVIRONMENT_JSON);

        assertEquals("  line one\nline two  ", response);
    }

    @Test
    void test_3() {
        RuntimeException exception = invokeExpectingRuntimeException(
                new TestIntelligenceBridge(),
                null,
                ENVIRONMENT_JSON);

        assertEquals("requestJson must not be null", exception.getMessage());
    }

    @Test
    void test_4() {
        RuntimeException exception = invokeExpectingRuntimeException(
                new TestIntelligenceBridge(),
                "   ",
                ENVIRONMENT_JSON);

        assertEquals("requestJson must not be blank", exception.getMessage());
    }

    @Test
    void test_5() {
        RuntimeException exception = invokeExpectingRuntimeException(
                new TestIntelligenceBridge(),
                "{",
                ENVIRONMENT_JSON);

        assertEquals("requestJson must contain valid JSON", exception.getMessage());
        assertInstanceOf(JsonParseException.class, exception.getCause());
    }

    @Test
    void test_6() {
        RuntimeException exception = invokeExpectingRuntimeException(
                new TestIntelligenceBridge(),
                REQUEST_JSON,
                null);

        assertEquals("environmentJson must not be null", exception.getMessage());
    }

    @Test
    void test_7() {
        RuntimeException exception = invokeExpectingRuntimeException(
                new TestIntelligenceBridge(),
                REQUEST_JSON,
                "   ");

        assertEquals("environmentJson must not be blank", exception.getMessage());
    }

    @Test
    void test_8() {
        RuntimeException exception = invokeExpectingRuntimeException(
                new TestIntelligenceBridge(),
                REQUEST_JSON,
                "{");

        assertEquals("environmentJson must contain valid JSON", exception.getMessage());
        assertInstanceOf(JsonParseException.class, exception.getCause());
    }

    @Test
    void test_9() {
        TestIntelligenceBridge bridge = successfulBridge();
        bridge.statusCode = 503;

        RuntimeException exception = invokeExpectingRuntimeException(
                bridge,
                REQUEST_JSON,
                ENVIRONMENT_JSON);

        assertEquals(
                "LiteLLM invocation failed with HTTP status 503",
                exception.getMessage());
    }

    @Test
    void test_10() {
        TestIntelligenceBridge bridge = new TestIntelligenceBridge();
        IOException failure = new IOException("controlled I/O failure");
        bridge.ioException = failure;

        RuntimeException exception = invokeExpectingRuntimeException(
                bridge,
                REQUEST_JSON,
                ENVIRONMENT_JSON);

        assertEquals("LiteLLM invocation failed", exception.getMessage());
        assertSame(failure, exception.getCause());
    }

    @Test
    void test_11() {
        TestIntelligenceBridge bridge = new TestIntelligenceBridge();
        InterruptedException failure = new InterruptedException("controlled interruption");
        bridge.interruptedException = failure;
        Thread.interrupted();

        try {
            RuntimeException exception = invokeExpectingRuntimeException(
                    bridge,
                    REQUEST_JSON,
                    ENVIRONMENT_JSON);

            assertEquals("LiteLLM invocation was interrupted", exception.getMessage());
            assertSame(failure, exception.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }

        assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    void test_12() {
        TestIntelligenceBridge bridge = new TestIntelligenceBridge();
        bridge.statusCode = 200;
        bridge.responseBody = null;

        RuntimeException exception = invokeExpectingRuntimeException(
                bridge,
                REQUEST_JSON,
                ENVIRONMENT_JSON);

        assertEquals("responseJson must not be null", exception.getMessage());
    }

    @Test
    void test_13() {
        TestIntelligenceBridge bridge = new TestIntelligenceBridge();
        bridge.statusCode = 200;
        bridge.responseBody = "   ";

        RuntimeException exception = invokeExpectingRuntimeException(
                bridge,
                REQUEST_JSON,
                ENVIRONMENT_JSON);

        assertEquals("responseJson must not be blank", exception.getMessage());
    }

    @Test
    void test_14() {
        TestIntelligenceBridge bridge = new TestIntelligenceBridge();
        bridge.statusCode = 200;
        bridge.responseBody = "{";

        RuntimeException exception = invokeExpectingRuntimeException(
                bridge,
                REQUEST_JSON,
                ENVIRONMENT_JSON);

        assertEquals("responseJson must contain valid JSON", exception.getMessage());
        assertInstanceOf(JsonParseException.class, exception.getCause());
    }

    @Test
    void test_15() {
        TestIntelligenceBridge bridge = successfulBridge();

        bridge.invoke(REQUEST_JSON, ENVIRONMENT_JSON);

        HttpRequest capturedRequest = bridge.capturedRequest;
        assertNotNull(capturedRequest);
        assertEquals(
                URI.create("http://localhost:4000/v1/chat/completions"),
                capturedRequest.uri());
        assertEquals("POST", capturedRequest.method());
        assertEquals(
                "application/json",
                capturedRequest.headers().firstValue("Content-Type").orElse(null));
        assertEquals(LITELLM_REQUEST_JSON, readRequestBody(capturedRequest));
    }

    @Test
    void test_16() {
        assertRequestFailure(
                "{\"prompt\":\"prompt\",\"stream\":false}",
                "requestJson.model is required");
    }

    @Test
    void test_17() {
        assertRequestFailure(
                "{\"model\":42,\"prompt\":\"prompt\",\"stream\":false}",
                "requestJson.model must be a string");
    }

    @Test
    void test_18() {
        assertRequestFailure(
                "{\"model\":\"model\",\"stream\":false}",
                "requestJson.prompt is required");
    }

    @Test
    void test_19() {
        assertRequestFailure(
                "{\"model\":\"model\",\"prompt\":42,\"stream\":false}",
                "requestJson.prompt must be a string");
    }

    @Test
    void test_20() {
        assertRequestFailure(
                "{\"model\":\"model\",\"prompt\":\"prompt\"}",
                "requestJson.stream is required");
    }

    @Test
    void test_21() {
        assertRequestFailure(
                "{\"model\":\"model\",\"prompt\":\"prompt\",\"stream\":\"false\"}",
                "requestJson.stream must be a boolean");
    }

    @Test
    void test_22() {
        assertResponseFailure(
                "{}",
                "responseJson.choices is required");
    }

    @Test
    void test_23() {
        assertResponseFailure(
                "{\"choices\":{}}",
                "responseJson.choices must be an array");
    }

    @Test
    void test_24() {
        assertResponseFailure(
                "{\"choices\":[]}",
                "responseJson.choices must not be empty");
    }

    @Test
    void test_25() {
        assertResponseFailure(
                "{\"choices\":[\"choice\"]}",
                "responseJson.choices[0] must be an object");
    }

    @Test
    void test_26() {
        assertResponseFailure(
                "{\"choices\":[{}]}",
                "responseJson.choices[0].message is required");
    }

    @Test
    void test_27() {
        assertResponseFailure(
                "{\"choices\":[{\"message\":\"message\"}]}",
                "responseJson.choices[0].message must be an object");
    }

    @Test
    void test_28() {
        assertResponseFailure(
                "{\"choices\":[{\"message\":{}}]}",
                "responseJson.choices[0].message.content is required");
    }

    @Test
    void test_29() {
        assertResponseFailure(
                "{\"choices\":[{\"message\":{\"content\":42}}]}",
                "responseJson.choices[0].message.content must be a string");
    }

    private TestIntelligenceBridge successfulBridge() {
        TestIntelligenceBridge bridge = new TestIntelligenceBridge();
        bridge.statusCode = 200;
        bridge.responseBody = RESPONSE_JSON;
        return bridge;
    }

    private void assertRequestFailure(String requestJson, String expectedMessage) {
        RuntimeException exception = invokeExpectingRuntimeException(
                new TestIntelligenceBridge(),
                requestJson,
                ENVIRONMENT_JSON);

        assertEquals(expectedMessage, exception.getMessage());
    }

    private void assertResponseFailure(String responseJson, String expectedMessage) {
        TestIntelligenceBridge bridge = new TestIntelligenceBridge();
        bridge.statusCode = 200;
        bridge.responseBody = responseJson;

        RuntimeException exception = invokeExpectingRuntimeException(
                bridge,
                REQUEST_JSON,
                ENVIRONMENT_JSON);

        assertEquals(expectedMessage, exception.getMessage());
    }

    private RuntimeException invokeExpectingRuntimeException(
            TestIntelligenceBridge bridge,
            String requestJson,
            String environmentJson) {

        RuntimeException capturedException = null;

        try {
            bridge.invoke(requestJson, environmentJson);
        } catch (RuntimeException exception) {
            capturedException = exception;
        }

        assertNotNull(capturedException);
        return capturedException;
    }

    private String readRequestBody(HttpRequest request) {
        Optional<HttpRequest.BodyPublisher> bodyPublisher = request.bodyPublisher();
        assertTrue(bodyPublisher.isPresent());

        RequestBodySubscriber subscriber = new RequestBodySubscriber();
        bodyPublisher.get().subscribe(subscriber);

        assertTrue(subscriber.completed);
        assertEquals(null, subscriber.failure);

        return subscriber.body.toString(StandardCharsets.UTF_8);
    }

    private static class TestIntelligenceBridge extends IntelligenceBridge {

        private int statusCode = 200;
        private String responseBody = "{}";
        private IOException ioException;
        private InterruptedException interruptedException;
        private HttpRequest capturedRequest;

        @Override
        HttpResponse<String> send(HttpRequest request)
                throws IOException, InterruptedException {

            capturedRequest = request;

            if (ioException != null) {
                throw ioException;
            }

            if (interruptedException != null) {
                throw interruptedException;
            }

            return new StubHttpResponse(request, statusCode, responseBody);
        }
    }

    private static class StubHttpResponse implements HttpResponse<String> {

        private final HttpRequest request;
        private final int statusCode;
        private final String body;

        private StubHttpResponse(
                HttpRequest request,
                int statusCode,
                String body) {

            this.request = request;
            this.statusCode = statusCode;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return request;
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            BiPredicate<String, String> filter = new BiPredicate<String, String>() {
                @Override
                public boolean test(String name, String value) {
                    return true;
                }
            };

            return HttpHeaders.of(Map.of(), filter);
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }

    private static class RequestBodySubscriber implements Flow.Subscriber<ByteBuffer> {

        private final ByteArrayOutputStream body = new ByteArrayOutputStream();
        private Throwable failure;
        private boolean completed;

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
            completed = true;
        }
    }
}
