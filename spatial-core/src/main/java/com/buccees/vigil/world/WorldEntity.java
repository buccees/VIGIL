package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Current, evidence-backed belief about a physical or logical entity. */
public record WorldEntity(
        String id,
        EntityType type,
        LocalPosition position,
        LocalPosition velocityMetersPerSecond,
        Confidence confidence,
        Instant lastUpdated,
        String sourceTrackId,
        List<String> detectionIds,
        TrackLifecycleState lifecycleState,
        WorldEntityValidity validity,
        WorldEntityFreshness freshness
) {
    public WorldEntity {
        requireText(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(velocityMetersPerSecond, "velocityMetersPerSecond");
        Objects.requireNonNull(confidence, "confidence");
        Objects.requireNonNull(lastUpdated, "lastUpdated");
        requireText(sourceTrackId, "sourceTrackId");
        detectionIds = List.copyOf(Objects.requireNonNull(detectionIds, "detectionIds"));
        if (detectionIds.isEmpty()) throw new IllegalArgumentException("detectionIds must not be empty");
        Objects.requireNonNull(lifecycleState, "lifecycleState");
        Objects.requireNonNull(validity, "validity");
        Objects.requireNonNull(freshness, "freshness");
    }

    /** Compatibility constructor for the original minimal world-model projection. */
    public WorldEntity(String id, EntityType type, LocalPosition position, Confidence confidence, Instant lastUpdated) {
        this(id, type, position, new LocalPosition(0.0, 0.0, 0.0), confidence, lastUpdated,
                "legacy", List.of("legacy"), TrackLifecycleState.CONFIRMED,
                WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
    }

    public WorldEntity withFreshness(WorldEntityFreshness newFreshness) {
        return new WorldEntity(id, type, position, velocityMetersPerSecond, confidence, lastUpdated,
                sourceTrackId, detectionIds, lifecycleState, validity, newFreshness);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
    }
}
