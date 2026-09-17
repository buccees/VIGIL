package com.buccees.vigil.spatial;

/** Deterministic spatial calculations used by higher-level VIGIL services. */
public final class SpatialMath {
    private SpatialMath() {}

    public static double distanceMeters(LocalPosition a, LocalPosition b) {
        return a.distanceTo(b);
    }

    /**
     * Returns a compass bearing in degrees clockwise from true north.
     * The local frame convention is x=east, y=north, z=up.
     */
    public static double bearingDegrees(LocalPosition from, LocalPosition to) {
        double east = to.xM() - from.xM();
        double north = to.yM() - from.yM();
        if (east == 0.0 && north == 0.0) {
            throw new IllegalArgumentException("Bearing is undefined for coincident positions");
        }
        double bearing = Math.toDegrees(Math.atan2(east, north));
        return (bearing + 360.0) % 360.0;
    }
}
