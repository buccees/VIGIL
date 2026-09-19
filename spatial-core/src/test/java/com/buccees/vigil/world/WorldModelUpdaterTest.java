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
    void equalTimestampTrackUpdatesResolveDeterministically() {
        Track first = new Track("track-1", EntityType.VEHICLE, new LocalPosition(1, 0, 0),
                new LocalPosition(0, 0, 0), new Confidence(0.8), T0, List.of("d1"), TrackLifecycleState.CONFIRMED);
        Track second = new Track("track-1", EntityType.VEHICLE, new LocalPosition(9, 0, 0),
                new LocalPosition(0, 0, 0), new Confidence(0.8), T0, List.of("d2"), TrackLifecycleState.CONFIRMED);

        WorldModel firstModel = new WorldModel();
        WorldModelUpdater firstUpdater = new WorldModelUpdater(firstModel);
        firstUpdater.update(first);
        WorldEntity firstResult = firstUpdater.update(second);

        WorldModel secondModel = new WorldModel();
        WorldModelUpdater secondUpdater = new WorldModelUpdater(secondModel);
        secondUpdater.update(second);
        WorldEntity secondResult = secondUpdater.update(first);

        assertEquals(firstResult, secondResult);
        assertEquals(firstResult, firstModel.find("entity-1").orElseThrow());
        assertEquals(secondResult, secondModel.find("entity-1").orElseThrow());
    }

    @Test
    void equalTimestampIdenticalTrackUpdateIsNotAStateChange() {
        Track track = new Track("track-1", EntityType.VEHICLE, new LocalPosition(1, 0, 0),
                new LocalPosition(0, 0, 0), new Confidence(0.8), T0, List.of("d1"), TrackLifecycleState.CONFIRMED);
        List<WorldModelEvent> events = new ArrayList<>();
        WorldModelUpdater updater = new WorldModelUpdater(new WorldModel(), events::add);

        updater.update(track);
        updater.update(track);

        assertEquals(1, events.size());
    }

    @Test
    void acceptedUpdatesEmitDeterministicEventsOnlyAfterStateChanges() {
        List<WorldModelEvent> firstEvents = new ArrayList<>();
        WorldModelUpdater firstUpdater = new WorldModelUpdater(new WorldModel(), firstEvents::add);
        Track first = new Track("track-1", EntityType.VEHICLE, new LocalPosition(1, 0, 0),
                new LocalPosition(0, 0, 0), new Confidence(0.8), T0, List.of("d1"), TrackLifecycleState.CONFIRMED);
        Track second = new Track("track-1", EntityType.VEHICLE, new LocalPosition(2, 0, 0),
                new LocalPosition(1, 0, 0), new Confidence(0.9), T0.plusSeconds(1), List.of("d2"), TrackLifecycleState.DEGRADED);
        firstUpdater.update(first);
        firstUpdater.update(second);
        firstUpdater.update(first);

        List<WorldModelEvent> secondEvents = new ArrayList<>();
        WorldModelUpdater secondUpdater = new WorldModelUpdater(new WorldModel(), secondEvents::add);
        secondUpdater.update(first);
        secondUpdater.update(second);
        secondUpdater.update(first);

        assertEquals(firstEvents, secondEvents);
        assertEquals(2, firstEvents.size());
        assertEquals("world-event-1", firstEvents.get(0).id());
        assertEquals("world-event-2", firstEvents.get(1).id());
        assertEquals(WorldModelEvent.Type.WORLD_ENTITY_CREATED, firstEvents.get(0).type());
        assertEquals(WorldModelEvent.Type.WORLD_ENTITY_BECAME_DEGRADED, firstEvents.get(1).type());
        assertEquals(TrackLifecycleState.CONFIRMED, firstEvents.get(0).stateAfter());
        assertEquals(TrackLifecycleState.DEGRADED, firstEvents.get(1).stateAfter());
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
