package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrackManagerTest {
    private static final Instant T0 = Instant.parse("2026-09-02T00:00:00Z");

    @Test
    void nearbyDetectionsBecomeOneTrackAndVelocityIsCalculated() {
        TrackManager manager = new TrackManager(5.0);
        Detection first = detection("d1", EntityType.VEHICLE, 0, 0, 0, T0);
        Detection second = detection("d2", EntityType.VEHICLE, 2, 0, 0, T0.plusSeconds(2));

        Track firstTrack = manager.update(first);
        Track secondTrack = manager.update(second);

        assertEquals(firstTrack.id(), secondTrack.id());
        assertEquals(new LocalPosition(1, 0, 0), secondTrack.velocityMetersPerSecond());
        assertEquals(2, secondTrack.detectionIds().size());
        assertEquals("d2", secondTrack.detectionIds().get(1));
    }

    @Test
    void distantDetectionsCreateSeparateTracks() {
        TrackManager manager = new TrackManager(5.0);

        Track first = manager.update(detection("d1", EntityType.PERSON, 0, 0, 0, T0));
        Track second = manager.update(detection("d2", EntityType.PERSON, 10, 0, 0, T0.plusSeconds(1)));

        assertNotEquals(first.id(), second.id());
        assertEquals(2, manager.snapshot().size());
    }

    @Test
    void differentTypesDoNotAssociate() {
        TrackManager manager = new TrackManager(5.0);

        Track vehicle = manager.update(detection("d1", EntityType.VEHICLE, 0, 0, 0, T0));
        Track person = manager.update(detection("d2", EntityType.PERSON, 0, 0, 0, T0.plusSeconds(1)));

        assertNotEquals(vehicle.id(), person.id());
        assertEquals(2, manager.snapshot().size());
    }

    @Test
    void invalidAssociationDistanceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new TrackManager(0.0));
        assertThrows(IllegalArgumentException.class, () -> new TrackManager(-1.0));
    }

    private static Detection detection(String id, EntityType type, double x, double y, double z, Instant time) {
        return new Detection(id, "observation-" + id, type,
                new LocalPosition(x, y, z), new Confidence(0.9), time);
    }
}
