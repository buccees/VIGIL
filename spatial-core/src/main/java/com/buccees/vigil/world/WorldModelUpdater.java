package com.buccees.vigil.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Controlled boundary that projects validated track state into the authoritative World Model. */
public final class WorldModelUpdater {
    private final WorldModel worldModel;
    private final WorldModelEventPublisher eventPublisher;
    private final Map<String, String> trackToEntity = new HashMap<>();
    private long nextEntityNumber = 1;
    private long nextEventNumber = 1;

    public WorldModelUpdater(WorldModel worldModel) {
        this(worldModel, event -> { });
    }

    public WorldModelUpdater(WorldModel worldModel, WorldModelEventPublisher eventPublisher) {
        this.worldModel = Objects.requireNonNull(worldModel, "worldModel");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    }

    /** Projects a valid track. Older track state is ignored and cannot overwrite newer state. */
    public synchronized WorldEntity update(Track track) {
        validate(track);

        String entityId = trackToEntity.get(track.id());
        if (entityId == null) {
            entityId = "entity-" + nextEntityNumber++;
            trackToEntity.put(track.id(), entityId);
        }

        WorldEntity current = worldModel.find(entityId).orElse(null);
        if (current != null && track.lastUpdated().isBefore(current.lastUpdated())) {
            return current;
        }

        WorldEntity next = toEntity(entityId, track);
        if (!worldModel.upsertIfNewer(next)) {
            return worldModel.find(entityId).orElse(next);
        }

        WorldModelEvent.Type eventType = current == null
                ? WorldModelEvent.Type.WORLD_ENTITY_CREATED
                : eventTypeFor(track.lifecycleState());
        eventPublisher.publish(new WorldModelEvent(
                "world-event-" + nextEventNumber++,
                track.lastUpdated(),
                entityId,
                track.id(),
                eventType,
                current == null ? null : current.lifecycleState(),
                track.lifecycleState(),
                track.detectionIds()));
        return next;
    }

    public synchronized Map<String, String> trackEntityAssociations() {
        return Map.copyOf(trackToEntity);
    }

    private static WorldEntity toEntity(String entityId, Track track) {
        WorldEntityValidity validity = switch (track.lifecycleState()) {
            case TENTATIVE, CONFIRMED -> WorldEntityValidity.VALID;
            case DEGRADED -> WorldEntityValidity.DEGRADED;
            case STALE, TERMINATED -> WorldEntityValidity.INVALID;
        };
        WorldEntityFreshness freshness = switch (track.lifecycleState()) {
            case STALE, TERMINATED -> WorldEntityFreshness.STALE;
            default -> WorldEntityFreshness.CURRENT;
        };
        return new WorldEntity(entityId, track.type(), track.position(), track.velocityMetersPerSecond(),
                track.confidence(), track.lastUpdated(), track.id(), track.detectionIds(),
                track.lifecycleState(), validity, freshness);
    }

    private static WorldModelEvent.Type eventTypeFor(TrackLifecycleState state) {
        return switch (state) {
            case DEGRADED -> WorldModelEvent.Type.WORLD_ENTITY_BECAME_DEGRADED;
            case STALE -> WorldModelEvent.Type.WORLD_ENTITY_BECAME_STALE;
            case TERMINATED -> WorldModelEvent.Type.WORLD_ENTITY_TERMINATED;
            default -> WorldModelEvent.Type.WORLD_ENTITY_UPDATED;
        };
    }

    private static void validate(Track track) {
        Objects.requireNonNull(track, "track");
        if (!Double.isFinite(track.position().xM()) || !Double.isFinite(track.position().yM()) || !Double.isFinite(track.position().zM())) {
            throw new IllegalArgumentException("track position must be finite");
        }
        if (!Double.isFinite(track.velocityMetersPerSecond().xM()) || !Double.isFinite(track.velocityMetersPerSecond().yM())
                || !Double.isFinite(track.velocityMetersPerSecond().zM())) {
            throw new IllegalArgumentException("track velocity must be finite");
        }
    }
}
