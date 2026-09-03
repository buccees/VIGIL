package com.buccees.vigil.world;

import com.buccees.vigil.fusion.FusedEstimate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Controlled boundary that projects validated track and fusion state into the authoritative World Model. */
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
            entityId = allocateEntityId();
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
        publishEvent(next, current, track.id(), eventType);
        return next;
    }

    /**
     * Projects a validated fused estimate through the same authoritative update boundary.
     * A fused estimate never writes the World Model directly and cannot merge two already
     * distinct entities implicitly; conflicting existing associations are rejected.
     */
    public synchronized WorldEntity update(FusedEstimate estimate) {
        Objects.requireNonNull(estimate, "estimate");
        validate(estimate);

        String entityId = resolveFusedEntity(estimate);
        WorldEntity current = worldModel.find(entityId).orElse(null);
        if (current != null && estimate.latestEventTime().isBefore(current.lastUpdated())) {
            return current;
        }

        WorldEntity next = toEntity(entityId, estimate);
        if (!worldModel.upsertIfNewer(next)) {
            return worldModel.find(entityId).orElse(next);
        }

        for (String trackId : estimate.trackIds()) {
            trackToEntity.put(trackId, entityId);
        }

        WorldModelEvent.Type eventType = current == null
                ? WorldModelEvent.Type.WORLD_ENTITY_CREATED
                : WorldModelEvent.Type.WORLD_ENTITY_UPDATED;
        publishEvent(next, current, estimate.associationId(), eventType);
        return next;
    }

    public synchronized Map<String, String> trackEntityAssociations() {
        return Map.copyOf(trackToEntity);
    }

    private String resolveFusedEntity(FusedEstimate estimate) {
        String resolvedEntityId = null;
        for (String trackId : estimate.trackIds()) {
            String candidate = trackToEntity.get(trackId);
            if (candidate == null) continue;
            if (resolvedEntityId == null) resolvedEntityId = candidate;
            else if (!resolvedEntityId.equals(candidate)) {
                throw new IllegalArgumentException("fused estimate references distinct existing entities");
            }
        }
        return resolvedEntityId != null ? resolvedEntityId : allocateEntityId();
    }

    private String allocateEntityId() {
        return "entity-" + nextEntityNumber++;
    }

    private void publishEvent(WorldEntity next, WorldEntity current, String trackId, WorldModelEvent.Type eventType) {
        eventPublisher.publish(new WorldModelEvent(
                "world-event-" + nextEventNumber++,
                next.lastUpdated(),
                next.id(),
                trackId,
                eventType,
                current == null ? null : current.lifecycleState(),
                next.lifecycleState(),
                next.detectionIds()));
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

    private static WorldEntity toEntity(String entityId, FusedEstimate estimate) {
        return new WorldEntity(entityId, estimate.type(), estimate.position(), estimate.velocityMetersPerSecond(),
                estimate.confidence(), estimate.latestEventTime(), estimate.associationId(), estimate.trackIds(),
                estimate.detectionIds(), TrackLifecycleState.CONFIRMED, WorldEntityValidity.VALID,
                WorldEntityFreshness.CURRENT);
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

    private static void validate(FusedEstimate estimate) {
        if (!Double.isFinite(estimate.position().xM()) || !Double.isFinite(estimate.position().yM())
                || !Double.isFinite(estimate.position().zM())) {
            throw new IllegalArgumentException("fused position must be finite");
        }
        if (!Double.isFinite(estimate.velocityMetersPerSecond().xM())
                || !Double.isFinite(estimate.velocityMetersPerSecond().yM())
                || !Double.isFinite(estimate.velocityMetersPerSecond().zM())) {
            throw new IllegalArgumentException("fused velocity must be finite");
        }
    }
}
