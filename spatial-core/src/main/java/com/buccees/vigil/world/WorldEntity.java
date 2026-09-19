package com.buccees.vigil.world;

import com.buccees.vigil.spatial.LocalPosition;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

/** Current, evidence-backed belief about a physical or logical entity. */
public record WorldEntity(
        String id,
        EntityType type,
        LocalPosition position,
        LocalPosition velocityMetersPerSecond,
        Confidence confidence,
        OptionalDouble positionUncertaintyMeters,
        Instant lastUpdated,
        String sourceTrackId,
        List<String> contributingTrackIds,
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
        Objects.requireNonNull(positionUncertaintyMeters, "positionUncertaintyMeters");
        Objects.requireNonNull(lastUpdated, "lastUpdated");
        contributingTrackIds = List.copyOf(Objects.requireNonNull(contributingTrackIds, "contributingTrackIds"));
        if (contributingTrackIds.isEmpty()) throw new IllegalArgumentException("contributingTrackIds must not be empty");
        detectionIds = List.copyOf(Objects.requireNonNull(detectionIds, "detectionIds"));
        if (detectionIds.isEmpty()) throw new IllegalArgumentException("detectionIds must not be empty");
        Objects.requireNonNull(lifecycleState, "lifecycleState");
        Objects.requireNonNull(validity, "validity");
        Objects.requireNonNull(freshness, "freshness");
        if (positionUncertaintyMeters.isPresent()) {
            double value = positionUncertaintyMeters.getAsDouble();
            if (!Double.isFinite(value) || value < 0.0) {
                throw new IllegalArgumentException("position uncertainty must be finite and non-negative");
            }
        }
    }

    /** Compatibility constructor for the original minimal world-model projection. */
    public WorldEntity(String id, EntityType type, LocalPosition position, Confidence confidence, Instant lastUpdated) {
        this(id, type, position, new LocalPosition(0.0, 0.0, 0.0), confidence, OptionalDouble.empty(), lastUpdated,
                "legacy", List.of("legacy"), List.of("legacy"), TrackLifecycleState.CONFIRMED,
                WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
    }

    /** Compatibility constructor preserving the pre-uncertainty full projection shape. */
    public WorldEntity(String id, EntityType type, LocalPosition position, LocalPosition velocityMetersPerSecond,
                       Confidence confidence, Instant lastUpdated, String sourceTrackId,
                       List<String> contributingTrackIds, List<String> detectionIds,
                       TrackLifecycleState lifecycleState, WorldEntityValidity validity,
                       WorldEntityFreshness freshness) {
        this(id, type, position, velocityMetersPerSecond, confidence, OptionalDouble.empty(), lastUpdated,
                sourceTrackId, contributingTrackIds, detectionIds, lifecycleState, validity, freshness);
    }

    public WorldEntity(String id, EntityType type, LocalPosition position, LocalPosition velocityMetersPerSecond,
                       Confidence confidence, Instant lastUpdated, String sourceTrackId,
                       List<String> detectionIds, TrackLifecycleState lifecycleState,
                       WorldEntityValidity validity, WorldEntityFreshness freshness) {
        this(id, type, position, velocityMetersPerSecond, confidence, OptionalDouble.empty(), lastUpdated, sourceTrackId,
                List.of(sourceTrackId), detectionIds, lifecycleState, validity, freshness);
    }

    public WorldEntity withFreshness(WorldEntityFreshness newFreshness) {
        return new WorldEntity(id, type, position, velocityMetersPerSecond, confidence, positionUncertaintyMeters,
                lastUpdated, sourceTrackId, contributingTrackIds, detectionIds, lifecycleState, validity, newFreshness);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
    }
}
