package com.buccees.vigil.world;

import com.buccees.vigil.fusion.FusedEstimate;
import com.buccees.vigil.spatial.LocalPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldModelUpdaterFusionTest {
    private static final Instant T0 = Instant.parse("2026-09-02T00:00:00Z");

    @Test
    void fusedEstimateCreatesOneEntityAndPreservesAllTrackProvenance() {
        WorldModel worldModel = new WorldModel();
        WorldModelUpdater updater = new WorldModelUpdater(worldModel);

        FusedEstimate estimate = estimate("track-a", List.of("track-a", "track-b"), T0.plusSeconds(2), 4.0);
        WorldEntity entity = updater.update(estimate);

        assertEquals(1, worldModel.snapshot().size());
        assertEquals(List.of("track-a", "track-b"), entity.contributingTrackIds());
        assertEquals(List.of("track-a-detection", "track-b-detection"), entity.detectionIds());
        assertEquals("track-a", entity.sourceTrackId());
        assertEquals(entity.id(), updater.trackEntityAssociations().get("track-a"));
        assertEquals(entity.id(), updater.trackEntityAssociations().get("track-b"));
    }

    @Test
    void olderFusedEstimateCannotOverwriteCurrentState() {
        WorldModel worldModel = new WorldModel();
        WorldModelUpdater updater = new WorldModelUpdater(worldModel);

        WorldEntity current = updater.update(estimate("track-a", List.of("track-a", "track-b"), T0.plusSeconds(2), 4.0));
        WorldEntity returned = updater.update(estimate("track-a", List.of("track-a", "track-b"), T0.plusSeconds(1), 1.0));

        assertEquals(current, returned);
        assertEquals(4.0, worldModel.find(current.id()).orElseThrow().position().xM());
    }

    @Test
    void fusedEstimateWithDistinctExistingEntitiesIsRejectedWithoutMutation() {
        WorldModel worldModel = new WorldModel();
        WorldModelUpdater updater = new WorldModelUpdater(worldModel);

        WorldEntity first = updater.update(estimate("track-a", List.of("track-a"), T0, 0.0));
        WorldEntity second = updater.update(estimate("track-b", List.of("track-b"), T0.plusSeconds(1), 10.0));

        assertEquals(2, worldModel.snapshot().size());
        assertThrows(IllegalArgumentException.class,
                () -> updater.update(estimate("track-a", List.of("track-a", "track-b"), T0.plusSeconds(2), 5.0)));
        assertEquals(first, worldModel.find(first.id()).orElseThrow());
        assertEquals(second, worldModel.find(second.id()).orElseThrow());
        assertEquals(2, worldModel.snapshot().size());
    }

    private static FusedEstimate estimate(String associationId, List<String> trackIds, Instant latestEvent, double x) {
        List<String> detections = trackIds.stream().map(id -> id + "-detection").toList();
        return new FusedEstimate(
                associationId,
                EntityType.VEHICLE,
                new LocalPosition(x, 0, 0),
                new LocalPosition(1, 0, 0),
                new Confidence(0.9),
                OptionalDouble.of(1.5),
                latestEvent.plusMillis(100),
                latestEvent,
                List.of("source-a", "source-b"),
                trackIds,
                detections,
                true,
                "test estimate");
    }
}
