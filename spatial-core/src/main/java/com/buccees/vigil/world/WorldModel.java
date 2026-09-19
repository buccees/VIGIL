package com.buccees.vigil.world;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Authoritative current spatial-state projection. */
public final class WorldModel {
    private final Map<String, WorldEntity> entities = new ConcurrentHashMap<>();

    /**
     * Controlled commit operation for WorldModelUpdater.
     * Package visibility prevents consumers in other packages from bypassing the update boundary.
     */
    synchronized boolean commitIfNewer(WorldEntity entity) {
        WorldEntity current = entities.get(entity.id());
        if (current != null && entity.lastUpdated().isBefore(current.lastUpdated())) {
            return false;
        }
        if (entity.equals(current)) {
            return false;
        }
        entities.put(entity.id(), entity);
        return true;
    }

    public Optional<WorldEntity> find(String id) {
        return Optional.ofNullable(entities.get(id));
    }

    /**
     * Recomputes derived freshness state for the current projection.
     * Package visibility keeps this mutation behind WorldModelUpdater.
     */
    synchronized void updateFreshness(Instant now, java.time.Duration agingAfter, java.time.Duration staleAfter) {
        if (now == null || agingAfter == null || staleAfter == null || agingAfter.isNegative() || staleAfter.compareTo(agingAfter) < 0) {
            throw new IllegalArgumentException("invalid freshness thresholds");
        }
        entities.replaceAll((id, entity) -> {
            java.time.Duration age = java.time.Duration.between(entity.lastUpdated(), now);
            WorldEntityFreshness freshness = age.compareTo(staleAfter) >= 0
                    ? WorldEntityFreshness.STALE
                    : age.compareTo(agingAfter) >= 0 ? WorldEntityFreshness.AGING : WorldEntityFreshness.CURRENT;
            return entity.freshness() == freshness ? entity : entity.withFreshness(freshness);
        });
    }

    /**
     * Returns a stable snapshot ordered by World Entity ID. The backing store is concurrent
     * and therefore does not provide iteration order; exposing that order would make replay,
     * tests, and downstream deterministic consumers depend on storage behavior.
     */
    public java.util.Collection<WorldEntity> snapshot() {
        return entities.values().stream()
                .sorted(java.util.Comparator.comparing(WorldEntity::id))
                .toList();
    }
}
