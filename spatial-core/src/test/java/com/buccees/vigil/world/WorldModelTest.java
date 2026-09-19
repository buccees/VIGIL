package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldModelTest {
    @Test
    void updaterIsTheAuthoritativeWriteBoundary() {
        WorldModel model = new WorldModel();
        WorldModelUpdater updater = new WorldModelUpdater(model);
        Instant now = Instant.parse("2026-09-02T00:00:00Z");
        Track firstTrack = new Track("track-1", EntityType.VEHICLE,
                new LocalPosition(1, 2, 0), new LocalPosition(0, 0, 0),
                new Confidence(0.8), now, java.util.List.of("d1"), TrackLifecycleState.CONFIRMED);
        Track secondTrack = new Track("track-1", EntityType.VEHICLE,
                new LocalPosition(3, 4, 0), new LocalPosition(0, 0, 0),
                new Confidence(0.9), now.plusSeconds(1), java.util.List.of("d2"), TrackLifecycleState.CONFIRMED);

        WorldEntity first = updater.update(firstTrack);
        WorldEntity second = updater.update(secondTrack);

        assertEquals(first.id(), second.id());
        assertEquals(second, model.find("entity-1").orElseThrow());
        assertEquals(1, model.snapshot().size());
    }

    @Test
    void missingEntityIsExplicitlyAbsent() {
        WorldModel model = new WorldModel();
        assertTrue(model.find("missing").isEmpty());
    }
}
