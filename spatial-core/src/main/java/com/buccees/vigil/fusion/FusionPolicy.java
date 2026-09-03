package com.buccees.vigil.fusion;

import java.time.Duration;
import java.util.Objects;

/** Explicit runtime policy for the initial deterministic fusion implementation. */
public record FusionPolicy(
        Duration maxEventTimeSkew,
        double maxAssociationDistanceMeters,
        double conflictDistanceMeters
) {
    public FusionPolicy {
        Objects.requireNonNull(maxEventTimeSkew, "maxEventTimeSkew");
        if (maxEventTimeSkew.isNegative()) {
            throw new IllegalArgumentException("maxEventTimeSkew must not be negative");
        }
        if (!Double.isFinite(maxAssociationDistanceMeters) || maxAssociationDistanceMeters < 0.0) {
            throw new IllegalArgumentException("maxAssociationDistanceMeters must be finite and non-negative");
        }
        if (!Double.isFinite(conflictDistanceMeters) || conflictDistanceMeters < maxAssociationDistanceMeters) {
            throw new IllegalArgumentException("conflictDistanceMeters must be finite and >= maxAssociationDistanceMeters");
        }
    }

    public static FusionPolicy conservativeDefaults() {
        return new FusionPolicy(Duration.ofMillis(250), 5.0, 20.0);
    }
}
