package com.buccees.vigil.world;

import com.buccees.vigil.fusion.FusedEstimate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Collections;

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
        WorldEntity next = toEntity(entityId, track, current);
        if (current != null && track.lastUpdated().equals(current.lastUpdated())) {
            next = resolveEqualTimestampTrackUpdate(current, next);
            if (next.equals(current)) {
                return current;
            }
        } else if (current != null && shouldReject(track.lastUpdated(), current, next)) {
            return current;
        }
        if (!worldModel.commitIfNewer(next)) {
            return worldModel.find(entityId).orElse(next);
        }

        WorldModelEvent.Type eventType = current == null
                ? WorldModelEvent.Type.WORLD_ENTITY_CREATED
                : eventTypeFor(track.lifecycleState());
        publishEvent(next, current, track.id(), null, WorldModelUpdateOrigin.TRACK, List.of(track.id()), eventType);
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
        WorldEntity next = toEntity(entityId, estimate);
        if (current != null && shouldReject(estimate.latestEventTime(), current, next)) {
            return current;
        }
        if (!worldModel.commitIfNewer(next)) {
            return worldModel.find(entityId).orElse(next);
        }

        for (String trackId : estimate.trackIds()) {
            trackToEntity.put(trackId, entityId);
        }

        WorldModelEvent.Type eventType = current == null
                ? WorldModelEvent.Type.WORLD_ENTITY_CREATED
                : WorldModelEvent.Type.WORLD_ENTITY_UPDATED;
        publishEvent(next, current, null, estimate.associationId(), WorldModelUpdateOrigin.FUSED_ESTIMATE,
                estimate.trackIds(), eventType);
        return next;
    }

    /**
     * Recomputes freshness through the same controlled boundary used for track and fusion
     * projections. Freshness is derived from authoritative event time, not ingestion order.
     */
    public synchronized void updateFreshness(Instant now, java.time.Duration agingAfter, java.time.Duration staleAfter) {
        Objects.requireNonNull(now, "now");
        Objects.requireNonNull(agingAfter, "agingAfter");
        Objects.requireNonNull(staleAfter, "staleAfter");
        if (agingAfter.isNegative() || staleAfter.compareTo(agingAfter) < 0) {
            throw new IllegalArgumentException("invalid freshness thresholds");
        }
        worldModel.updateFreshness(now, agingAfter, staleAfter);
    }

    /**
     * Returns a read-only association snapshot with stable Track-ID ordering.
     * The association state is externally observable and must not depend on HashMap
     * iteration behavior during replay, testing, or downstream serialization.
     */
    public synchronized Map<String, String> trackEntityAssociations() {
        return Collections.unmodifiableMap(new TreeMap<>(trackToEntity));
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

    private void publishEvent(WorldEntity next, WorldEntity current, String sourceTrackId, String associationId,
                              WorldModelUpdateOrigin origin, List<String> contributingTrackIds,
                              WorldModelEvent.Type eventType) {
        eventPublisher.publish(new WorldModelEvent(
                "world-event-" + nextEventNumber++,
                next.lastUpdated(),
                next.id(),
                sourceTrackId,
                associationId,
                origin,
                eventType,
                current == null ? null : current.lifecycleState(),
                next.lifecycleState(),
                contributingTrackIds,
                next.detectionIds()));
    }

    private static WorldEntity toEntity(String entityId, Track track, WorldEntity current) {
        WorldEntityValidity validity = switch (track.lifecycleState()) {
            case TENTATIVE, CONFIRMED -> WorldEntityValidity.VALID;
            case DEGRADED -> WorldEntityValidity.DEGRADED;
            case STALE, TERMINATED -> WorldEntityValidity.INVALID;
        };
        WorldEntityFreshness freshness = switch (track.lifecycleState()) {
            case STALE, TERMINATED -> WorldEntityFreshness.STALE;
            default -> WorldEntityFreshness.CURRENT;
        };
        List<String> contributingTrackIds = current == null
                ? List.of(track.id())
                : mergeProvenance(current.contributingTrackIds(), track.id());
        List<String> detectionIds = current == null
                ? track.detectionIds()
                : mergeProvenance(current.detectionIds(), track.detectionIds());
        String sourceTrackId = current == null ? track.id() : current.sourceTrackId();
        return new WorldEntity(entityId, track.type(), track.position(), track.velocityMetersPerSecond(),
                track.confidence(), java.util.OptionalDouble.empty(), track.lastUpdated(), sourceTrackId,
                contributingTrackIds, detectionIds, track.lifecycleState(), validity, freshness);
    }

    private static List<String> mergeProvenance(List<String> existing, String value) {
        return java.util.stream.Stream.concat(existing.stream(), java.util.stream.Stream.of(value))
                .distinct().toList();
    }

    private static List<String> mergeProvenance(List<String> existing, List<String> values) {
        return java.util.stream.Stream.concat(existing.stream(), values.stream())
                .distinct().toList();
    }

    /**
     * Equal-timestamp track updates are resolved deterministically for state fields while
     * retaining the union of all evidence provenance. Provenance order records first-seen
     * evidence, whereas the state winner is selected independently of arrival order.
     */
    private static WorldEntity resolveEqualTimestampTrackUpdate(WorldEntity current, WorldEntity candidate) {
        WorldEntity winner = deterministicStateKey(candidate).compareTo(deterministicStateKey(current)) > 0
                ? candidate : current;
        List<String> contributingTrackIds = mergeProvenance(current.contributingTrackIds(), candidate.contributingTrackIds());
        List<String> detectionIds = mergeProvenance(current.detectionIds(), candidate.detectionIds());
        return new WorldEntity(winner.id(), winner.type(), winner.position(), winner.velocityMetersPerSecond(),
                winner.confidence(), winner.positionUncertaintyMeters(), winner.lastUpdated(), winner.sourceTrackId(),
                contributingTrackIds, detectionIds, winner.lifecycleState(), winner.validity(), winner.freshness());
    }

    private static WorldEntity toEntity(String entityId, FusedEstimate estimate) {
        return new WorldEntity(entityId, estimate.type(), estimate.position(), estimate.velocityMetersPerSecond(),
                estimate.confidence(), estimate.positionUncertaintyMeters(), estimate.latestEventTime(), null, estimate.trackIds(),
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

    /**
     * Equal timestamps are resolved by a stable state key rather than arrival order.
     * This keeps authoritative state deterministic for replayed or concurrently produced
     * inputs that carry the same event time.
     */
    private static boolean shouldReject(java.time.Instant incomingTime, WorldEntity current, WorldEntity candidate) {
        int timeComparison = incomingTime.compareTo(current.lastUpdated());
        if (timeComparison < 0) return true;
        if (timeComparison > 0) return false;
        return deterministicStateKey(candidate).compareTo(deterministicStateKey(current)) <= 0;
    }

    private static String deterministicStateKey(WorldEntity entity) {
        return entity.type() + "|" + entity.position() + "|" + entity.velocityMetersPerSecond()
                + "|" + entity.confidence() + "|" + entity.positionUncertaintyMeters()
                + "|" + entity.lastUpdated() + "|" + entity.sourceTrackId()
                + "|" + entity.contributingTrackIds() + "|" + entity.detectionIds()
                + "|" + entity.lifecycleState() + "|" + entity.validity() + "|" + entity.freshness();
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
