package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

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
                new Confidence(0.8), now, List.of("d1"), TrackLifecycleState.CONFIRMED);
        Track secondTrack = new Track("track-1", EntityType.VEHICLE,
                new LocalPosition(3, 4, 0), new LocalPosition(0, 0, 0),
                new Confidence(0.9), now.plusSeconds(1), List.of("d2"), TrackLifecycleState.CONFIRMED);

        WorldEntity first = updater.update(firstTrack);
        WorldEntity second = updater.update(secondTrack);

        assertEquals(first.id(), second.id());
        assertEquals(second, model.find("entity-1").orElseThrow());
        assertEquals(1, model.snapshot().size());
    }

    @Test
    void snapshotIsDeterministicallyOrderedByEntityId() {
        WorldModel model = new WorldModel();
        WorldModelUpdater updater = new WorldModelUpdater(model);
        Instant now = Instant.parse("2026-09-02T00:00:00Z");

        updater.update(new Track("track-b", EntityType.VEHICLE, new LocalPosition(2, 0, 0),
                new LocalPosition(0, 0, 0), new Confidence(0.8), now, List.of("d2"), TrackLifecycleState.CONFIRMED));
        updater.update(new Track("track-a", EntityType.VEHICLE, new LocalPosition(1, 0, 0),
                new LocalPosition(0, 0, 0), new Confidence(0.8), now, List.of("d1"), TrackLifecycleState.CONFIRMED));

        assertEquals(List.of("entity-1", "entity-2"),
                model.snapshot().stream().map(WorldEntity::id).toList());
    }

    @Test
    void freshnessIsUpdatedThroughTheUpdaterBoundary() {
        WorldModel model = new WorldModel();
        WorldModelUpdater updater = new WorldModelUpdater(model);
        Instant eventTime = Instant.parse("2026-09-02T00:00:00Z");
        WorldEntity entity = updater.update(new Track("track-1", EntityType.VEHICLE,
                new LocalPosition(1, 0, 0), new LocalPosition(0, 0, 0),
                new Confidence(0.8), eventTime, List.of("d1"), TrackLifecycleState.CONFIRMED));

        updater.updateFreshness(eventTime.plusSeconds(6), Duration.ofSeconds(5), Duration.ofSeconds(10));
        assertEquals(WorldEntityFreshness.AGING, model.find(entity.id()).orElseThrow().freshness());

        updater.updateFreshness(eventTime.plusSeconds(11), Duration.ofSeconds(5), Duration.ofSeconds(10));
        assertEquals(WorldEntityFreshness.STALE, model.find(entity.id()).orElseThrow().freshness());
    }

    @Test
    void freshnessThresholdsMustBeValid() {
        WorldModelUpdater updater = new WorldModelUpdater(new WorldModel());
        Instant now = Instant.parse("2026-09-02T00:00:00Z");

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> updater.updateFreshness(now, Duration.ofSeconds(-1), Duration.ofSeconds(10)));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> updater.updateFreshness(now, Duration.ofSeconds(10), Duration.ofSeconds(5)));
    }

    @Test
    void missingEntityIsExplicitlyAbsent() {
        WorldModel model = new WorldModel();
        assertTrue(model.find("missing").isEmpty());
    }
}
