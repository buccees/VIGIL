package com.buccees.vigil.relevance;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AttentionPresentationPolicyTest {
    @Test
    void highPriorityIsPresented() {
        var decision = new AttentionPresentationPolicy(.8, .4)
                .decide("entity-1", new PriorityAssessment(.9, java.util.List.of("relevance"), "high"));
        assertEquals(PresentationAction.PRESENT, decision.action());
    }

    @Test
    void middlePriorityIsDeferred() {
        var decision = new AttentionPresentationPolicy(.8, .4)
                .decide("entity-1", new PriorityAssessment(.6, java.util.List.of("relevance"), "middle"));
        assertEquals(PresentationAction.DEFER, decision.action());
    }

    @Test
    void lowPriorityIsSuppressed() {
        var decision = new AttentionPresentationPolicy(.8, .4)
                .decide("entity-1", new PriorityAssessment(.2, java.util.List.of("relevance"), "low"));
        assertEquals(PresentationAction.SUPPRESS, decision.action());
    }

    @Test
    void policyOnlyDecidesPresentation() {
        var priority = new PriorityAssessment(.8, java.util.List.of("relevance"), "high");
        var decision = new AttentionPresentationPolicy(.8, .4).decide("entity-1", priority);
        assertEquals(.8, decision.priority());
        assertEquals("entity-1", decision.entityId());
    }
}
