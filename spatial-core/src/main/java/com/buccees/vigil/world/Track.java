package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Persistent identity for a sequence of detections believed to describe the same entity. */
public record Track(
        String id,
        EntityType type,
        LocalPosition position,
        LocalPosition velocityMetersPerSecond,
        Confidence confidence,
        Instant lastUpdated,
        List<String> detectionIds,
        TrackLifecycleState lifecycleState
) {
    public Track {
        requireText(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(velocityMetersPerSecond, "velocityMetersPerSecond");
        Objects.requireNonNull(confidence, "confidence");
        Objects.requireNonNull(lastUpdated, "lastUpdated");
        detectionIds = List.copyOf(Objects.requireNonNull(detectionIds, "detectionIds"));
        if (detectionIds.isEmpty()) {
            throw new IllegalArgumentException("detectionIds must not be empty");
        }
        Objects.requireNonNull(lifecycleState, "lifecycleState");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
