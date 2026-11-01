package ai.braineous.arc.prototype;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ExtractCognitiveOutcomeTest {

    @Test
    void bareReady() {
        assertEquals("READY", ArcPrototypeTest.extractCognitiveOutcome("READY"));
    }

    @Test
    void bareReview() {
        assertEquals("REVIEW", ArcPrototypeTest.extractCognitiveOutcome("REVIEW"));
    }

    @Test
    void boldToken() {
        assertEquals("READY", ArcPrototypeTest.extractCognitiveOutcome("**READY**"));
    }

    @Test
    void backtickWrappedToken() {
        assertEquals("REVIEW", ArcPrototypeTest.extractCognitiveOutcome("`REVIEW`"));
    }

    @Test
    void fencedToken() {
        assertEquals("READY", ArcPrototypeTest.extractCognitiveOutcome("```\nREADY\n```"));
    }

    @Test
    void quotedToken() {
        assertEquals("REVIEW", ArcPrototypeTest.extractCognitiveOutcome("\"REVIEW\""));
    }

    @Test
    void existingJsonResultExtraction() {
        assertEquals(
                "READY",
                ArcPrototypeTest.extractCognitiveOutcome("{\"result\":\"READY\"}"));
        assertEquals(
                "READY",
                ArcPrototypeTest.extractResultValue("{\"result\":\"READY\"}"));
    }

    @Test
    void unknownOutOfDomainToken() {
        assertEquals(
                "unavailable",
                ArcPrototypeTest.extractCognitiveOutcome("unavailable"));
    }

    @Test
    void unrecognizedProseIsNull() {
        assertNull(ArcPrototypeTest.extractCognitiveOutcome("the item is READY now"));
    }
}
