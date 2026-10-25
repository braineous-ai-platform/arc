package ai.braineous.arc.prototype;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import ai.braineous.arc.IntelligenceBridge;

class ArcPrototypeTest {

    @Test
    void test_1() throws Exception {
        InputStream stream =
                ArcPrototypeTest.class.getResourceAsStream(
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

        int iterations = 5;
        IntelligenceBridge intelligenceBridge = new IntelligenceBridge();

        System.out.print("=== INTELLIGENCEBRIDGE REQUEST BEGIN ===\n");
        System.out.print(requestJson);
        System.out.print("\n=== INTELLIGENCEBRIDGE REQUEST END ===\n");

        ExecutorService executor = Executors.newFixedThreadPool(iterations);
        List<Future<?>> futures = new ArrayList<Future<?>>();

        for (int i = 1; i <= iterations; i++) {
            final int iteration = i;
            futures.add(executor.submit(() -> {
                System.out.print("=== RUN " + iteration + " BEGIN ===\n");
                String response;
                try {
                    response =
                            intelligenceBridge.invoke(
                                    requestJson,
                                    environmentJson);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    throw new RuntimeException(exception);
                }

                System.out.print("=== INTELLIGENCEBRIDGE RESPONSE BEGIN ===\n");
                System.out.print(response);
                System.out.print("\n=== INTELLIGENCEBRIDGE RESPONSE END ===\n");

                String extracted = extractResultValue(response);
                boolean accepted = false;
                if (extracted != null) {
                    if (extracted.equals("ELIGIBLE")) {
                        accepted = true;
                    } else {
                        if (extracted.equals("result")) {
                            accepted = true;
                        }
                    }
                }
                if (!accepted) {
                    throw new RuntimeException(
                            "V14 result assertion failed\n"
                                    + "=== INTELLIGENCEBRIDGE RESPONSE BEGIN ===\n"
                                    + response
                                    + "\n=== INTELLIGENCEBRIDGE RESPONSE END ===\n");
                }
                System.out.print("=== RUN " + iteration + " END ===\n");
            }));
        }

        try {
            for (int i = 0; i < futures.size(); i++) {
                futures.get(i).get();
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        }
    }

    static String extractResultValue(String response) {
        if (response == null) {
            return null;
        }

        String content = unwrapMarkdownFence(response);
        if (content == null) {
            return null;
        }
        content = content.trim();
        if (content.length() == 0) {
            return null;
        }

        if (content.charAt(0) == '{') {
            return extractJsonResult(content);
        }
        return extractKeyValueResult(content);
    }

    static String unwrapMarkdownFence(String response) {
        String trimmed = response.trim();
        int openLen = 0;
        if (trimmed.startsWith("````")) {
            openLen = 4;
        } else {
            if (trimmed.startsWith("```")) {
                openLen = 3;
            }
        }
        if (openLen == 0) {
            return trimmed;
        }

        int pos = openLen;
        if (pos < trimmed.length()) {
            char first = trimmed.charAt(pos);
            if (first == '\n') {
                pos++;
            } else {
                if (first == '\r') {
                    pos++;
                    if (pos < trimmed.length()) {
                        if (trimmed.charAt(pos) == '\n') {
                            pos++;
                        }
                    }
                } else {
                    if (Character.isLetter(first)) {
                        while (pos < trimmed.length()) {
                            char current = trimmed.charAt(pos);
                            if (current == '\n') {
                                pos++;
                                break;
                            }
                            if (current == '\r') {
                                pos++;
                                if (pos < trimmed.length()) {
                                    if (trimmed.charAt(pos) == '\n') {
                                        pos++;
                                    }
                                }
                                break;
                            }
                            pos++;
                        }
                    }
                }
            }
        }

        String afterOpen = trimmed.substring(pos);
        String fence;
        if (openLen == 4) {
            fence = "````";
        } else {
            fence = "```";
        }
        if (afterOpen.endsWith(fence)) {
            afterOpen = afterOpen.substring(0, afterOpen.length() - fence.length());
        }
        return afterOpen.trim();
    }

    static String extractJsonResult(String content) {
        JsonElement element;
        try {
            element = JsonParser.parseString(content);
        } catch (Exception exception) {
            return null;
        }
        if (element == null) {
            return null;
        }
        if (!element.isJsonObject()) {
            return null;
        }

        JsonObject object = element.getAsJsonObject();
        if (object.size() != 1) {
            return null;
        }
        if (!object.has("result")) {
            return null;
        }

        JsonElement resultElement = object.get("result");
        if (resultElement == null) {
            return null;
        }
        if (resultElement.isJsonNull()) {
            return null;
        }
        if (!resultElement.isJsonPrimitive()) {
            return null;
        }

        JsonPrimitive primitive = resultElement.getAsJsonPrimitive();
        if (!primitive.isString()) {
            return null;
        }
        return primitive.getAsString();
    }

    static String extractKeyValueResult(String content) {
        if (!content.startsWith("result")) {
            return null;
        }

        int index = "result".length();
        while (index < content.length()) {
            if (!Character.isWhitespace(content.charAt(index))) {
                break;
            }
            index++;
        }
        if (index >= content.length()) {
            return null;
        }

        char separator = content.charAt(index);
        if (separator != '=') {
            if (separator != ':') {
                return null;
            }
        }
        index++;

        while (index < content.length()) {
            if (!Character.isWhitespace(content.charAt(index))) {
                break;
            }
            index++;
        }
        if (index >= content.length()) {
            return null;
        }

        String value;
        if (content.charAt(index) == '"') {
            int endQuote = -1;
            int cursor = index + 1;
            while (cursor < content.length()) {
                if (content.charAt(cursor) == '"') {
                    endQuote = cursor;
                    break;
                }
                cursor++;
            }
            if (endQuote < 0) {
                return null;
            }
            value = content.substring(index + 1, endQuote);
            index = endQuote + 1;
        } else {
            int end = index;
            while (end < content.length()) {
                if (Character.isWhitespace(content.charAt(end))) {
                    break;
                }
                end++;
            }
            value = content.substring(index, end);
            index = end;
        }

        while (index < content.length()) {
            if (!Character.isWhitespace(content.charAt(index))) {
                return null;
            }
            index++;
        }
        if (value.length() == 0) {
            return null;
        }
        return value;
    }
}
