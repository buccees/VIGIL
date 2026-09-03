package com.buccees.vigil.spatial;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpatialMathTest {
    @Test
    void calculatesThreeDimensionalDistance() {
        assertEquals(5.0, SpatialMath.distanceMeters(
                new LocalPosition(0, 0, 0),
                new LocalPosition(3, 4, 0)), 1e-9);
    }

    @Test
    void calculatesCompassBearing() {
        LocalPosition origin = new LocalPosition(0, 0, 0);
        assertEquals(0.0, SpatialMath.bearingDegrees(origin, new LocalPosition(0, 10, 0)), 1e-9);
        assertEquals(90.0, SpatialMath.bearingDegrees(origin, new LocalPosition(10, 0, 0)), 1e-9);
        assertEquals(180.0, SpatialMath.bearingDegrees(origin, new LocalPosition(0, -10, 0)), 1e-9);
        assertEquals(270.0, SpatialMath.bearingDegrees(origin, new LocalPosition(-10, 0, 0)), 1e-9);
    }

    @Test
    void rejectsUndefinedBearing() {
        LocalPosition origin = new LocalPosition(0, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> SpatialMath.bearingDegrees(origin, origin));
    }
}
