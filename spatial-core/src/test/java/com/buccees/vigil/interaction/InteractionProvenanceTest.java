package com.buccees.vigil.interaction;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class InteractionProvenanceTest {
    private static final Instant OBSERVED = Instant.parse("2026-09-18T14:00:00Z");

    @Test
    void provenancePreservesSourcesAndInterpretation() {
        InteractionProvenance p = new InteractionProvenance(
                "req-1", "session-1", List.of("entity-7", "entity-9"),
                OBSERVED, OBSERVED.plusSeconds(2), "fused observation summary");

        assertEquals(List.of("entity-7", "entity-9"), p.sourceEntityIds());
        assertEquals("fused observation summary", p.interpretation());
    }

    @Test
    void freshnessIsExplicitAndTimeBounded() {
        InteractionProvenance p = new InteractionProvenance(
                "req-2", "session-1", List.of("entity-7"),
                OBSERVED, OBSERVED.plusSeconds(1), "current observation");

        assertTrue(p.isFreshAt(OBSERVED.plusSeconds(30), Duration.ofMinutes(1)));
        assertFalse(p.isFreshAt(OBSERVED.plusSeconds(61), Duration.ofMinutes(1)));
    }

    @Test
    void responseCannotPredateItsSourceObservation() {
        assertThrows(IllegalArgumentException.class, () -> new InteractionProvenance(
                "req-3", "session-1", List.of("entity-7"),
                OBSERVED, OBSERVED.minusSeconds(1), "invalid chronology"));
    }
}
