package com.buccees.vigil.fusion;

import com.buccees.vigil.spatial.LocalPosition;
import com.buccees.vigil.world.Confidence;
import com.buccees.vigil.world.EntityType;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

/** Derived spatial estimate. It is not authoritative World Model state. */
public record FusedEstimate(
        String associationId,
        EntityType type,
        LocalPosition position,
        LocalPosition velocityMetersPerSecond,
        Confidence confidence,
        OptionalDouble positionUncertaintyMeters,
        Instant fusionTime,
        Instant latestEventTime,
        List<String> sourceIds,
        List<String> trackIds,
        List<String> detectionIds,
        boolean qualified,
        String qualityNote
) {
    public FusedEstimate {
        requireText(associationId, "associationId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(velocityMetersPerSecond, "velocityMetersPerSecond");
        Objects.requireNonNull(confidence, "confidence");
        Objects.requireNonNull(positionUncertaintyMeters, "positionUncertaintyMeters");
        Objects.requireNonNull(fusionTime, "fusionTime");
        Objects.requireNonNull(latestEventTime, "latestEventTime");
        sourceIds = List.copyOf(Objects.requireNonNull(sourceIds, "sourceIds"));
        trackIds = List.copyOf(Objects.requireNonNull(trackIds, "trackIds"));
        detectionIds = List.copyOf(Objects.requireNonNull(detectionIds, "detectionIds"));
        if (trackIds.isEmpty()) throw new IllegalArgumentException("trackIds must not be empty");
        Objects.requireNonNull(qualityNote, "qualityNote");
        if (positionUncertaintyMeters.isPresent()) {
            double value = positionUncertaintyMeters.getAsDouble();
            if (!Double.isFinite(value) || value < 0.0) {
                throw new IllegalArgumentException("position uncertainty must be finite and non-negative");
            }
        }
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
    }
}
