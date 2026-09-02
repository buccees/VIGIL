package com.buccees.vigil.world;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** A state-change event emitted only after the world model has been mutated. */
public record WorldModelEvent(
        String id,
        Instant eventTime,
        String entityId,
        String trackId,
        Type type,
        TrackLifecycleState stateBefore,
        TrackLifecycleState stateAfter,
        List<String> detectionIds
) {
    public enum Type {
        WORLD_ENTITY_CREATED,
        WORLD_ENTITY_UPDATED,
        WORLD_ENTITY_BECAME_DEGRADED,
        WORLD_ENTITY_BECAME_STALE,
        WORLD_ENTITY_TERMINATED
    }

    public WorldModelEvent {
        requireText(id, "id");
        Objects.requireNonNull(eventTime, "eventTime");
        requireText(entityId, "entityId");
        requireText(trackId, "trackId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(stateAfter, "stateAfter");
        detectionIds = List.copyOf(Objects.requireNonNull(detectionIds, "detectionIds"));
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
