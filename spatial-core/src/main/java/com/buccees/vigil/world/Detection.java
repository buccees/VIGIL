package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import java.time.Instant;
import java.util.Objects;

/** A perception result derived from one or more observations. */
public record Detection(
        String id,
        String observationId,
        EntityType type,
        LocalPosition position,
        Confidence confidence,
        Instant detectedAt
) {
    public Detection {
        requireText(id, "id");
        requireText(observationId, "observationId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(confidence, "confidence");
        Objects.requireNonNull(detectedAt, "detectedAt");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
