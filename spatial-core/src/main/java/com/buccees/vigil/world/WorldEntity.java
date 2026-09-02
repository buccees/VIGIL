package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import java.time.Instant;
import java.util.Objects;

/** Current, evidence-backed belief about a physical or logical entity. */
public record WorldEntity(
        String id,
        EntityType type,
        LocalPosition position,
        Confidence confidence,
        Instant lastUpdated
) {
    public WorldEntity {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must not be blank");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(confidence, "confidence");
        Objects.requireNonNull(lastUpdated, "lastUpdated");
    }
}
