package com.buccees.vigil.spatial;

/** Geographic position in decimal degrees and meters above a documented datum. */
public record GeoPoint(double latitudeDeg, double longitudeDeg, double altitudeM) {
    public GeoPoint {
        if (latitudeDeg < -90.0 || latitudeDeg > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        if (longitudeDeg < -180.0 || longitudeDeg > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
        if (!Double.isFinite(altitudeM)) {
            throw new IllegalArgumentException("Altitude must be finite");
        }
    }
}
