package com.buccees.vigil.spatial;

/** Position in a local Cartesian world frame, expressed in meters. */
public record LocalPosition(double xM, double yM, double zM) {
    public LocalPosition {
        if (!Double.isFinite(xM) || !Double.isFinite(yM) || !Double.isFinite(zM)) {
            throw new IllegalArgumentException("Local coordinates must be finite");
        }
    }

    public double distanceTo(LocalPosition other) {
        double dx = other.xM - xM;
        double dy = other.yM - yM;
        double dz = other.zM - zM;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
