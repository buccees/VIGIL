package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldModelTest {
    @Test
    void storesAndReplacesCurrentEntityBelief() {
        WorldModel model = new WorldModel();
        Instant now = Instant.parse("2026-09-02T00:00:00Z");
        WorldEntity first = new WorldEntity("entity-1", EntityType.VEHICLE,
                new LocalPosition(1, 2, 0), new Confidence(0.8), now);
        WorldEntity second = new WorldEntity("entity-1", EntityType.VEHICLE,
                new LocalPosition(3, 4, 0), new Confidence(0.9), now.plusSeconds(1));

        model.upsert(first);
        assertEquals(first, model.find("entity-1").orElseThrow());

        model.upsert(second);
        assertEquals(second, model.find("entity-1").orElseThrow());
        assertEquals(1, model.snapshot().size());
    }

    @Test
    void missingEntityIsExplicitlyAbsent() {
        WorldModel model = new WorldModel();
        assertTrue(model.find("missing").isEmpty());
    }
}
