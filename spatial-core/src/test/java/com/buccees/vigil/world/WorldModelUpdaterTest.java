package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldModelUpdaterTest {
    private static final Instant T0 = Instant.parse("2026-09-02T00:00:00Z");

    @Test
    void firstTrackCreatesOneEntityAndSecondUpdateReusesIt() {
        WorldModel model = new WorldModel();
        List<WorldModelEvent> events = new ArrayList<>();
        WorldModelUpdater updater = new WorldModelUpdater(model, events::add);
        TrackManager tracks = new TrackManager(5.0);

        Track first = tracks.update(detection("d1", 0, 0, 0, T0));
        Track second = tracks.update(detection("d2", 2, 0, 0, T0.plusSeconds(2)));

        WorldEntity firstEntity = updater.update(first);
        WorldEntity secondEntity = updater.update(second);

        assertEquals(firstEntity.id(), secondEntity.id());
        assertEquals(1, model.snapshot().size());
        assertEquals(new LocalPosition(1, 0, 0), secondEntity.velocityMetersPerSecond());
        assertEquals(List.of("d1", "d2"), secondEntity.detectionIds());
        assertEquals(2, events.size());
        assertEquals(WorldModelEvent.Type.WORLD_ENTITY_CREATED, events.get(0).type());
        assertEquals(WorldModelEvent.Type.WORLD_ENTITY_UPDATED, events.get(1).type());
    }

    @Test
    void olderTrackCannotOverwriteNewerWorldState() {
        WorldModel model = new WorldModel();
        List<WorldModelEvent> events = new ArrayList<>();
        WorldModelUpdater updater = new WorldModelUpdater(model, events::add);
        TrackManager tracks = new TrackManager(5.0);

        Track first = tracks.update(detection("d1", 0, 0, 0, T0));
        Track newer = tracks.update(detection("d2", 2, 0, 0, T0.plusSeconds(2)));
        updater.update(first);
        updater.update(newer);

        WorldEntity before = model.find("entity-1").orElseThrow();
        updater.update(first);
        WorldEntity after = model.find("entity-1").orElseThrow();

        assertEquals(before, after);
        assertEquals(2, events.size());
    }

    @Test
    void lifecycleIsProjectedWithoutDeletingIdentity() {
        WorldModel model = new WorldModel();
        List<WorldModelEvent> events = new ArrayList<>();
        WorldModelUpdater updater = new WorldModelUpdater(model, events::add);
        Track track = new Track("track-1", EntityType.PERSON, new LocalPosition(1, 2, 0),
                new LocalPosition(0, 0, 0), new Confidence(0.7), T0, List.of("d1"),
                TrackLifecycleState.STALE);

        WorldEntity entity = updater.update(track);

        assertEquals(TrackLifecycleState.STALE, entity.lifecycleState());
        assertEquals(WorldEntityValidity.INVALID, entity.validity());
        assertEquals(WorldEntityFreshness.STALE, entity.freshness());
        assertTrue(model.find(entity.id()).isPresent());
        assertEquals(WorldModelEvent.Type.WORLD_ENTITY_CREATED, events.get(0).type());
    }

    private static Detection detection(String id, double x, double y, double z, Instant time) {
        return new Detection(id, "observation-" + id, EntityType.VEHICLE,
                new LocalPosition(x, y, z), new Confidence(0.9), time);
    }
}
