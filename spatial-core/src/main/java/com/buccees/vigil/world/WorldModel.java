package com.buccees.vigil.world;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Minimal in-memory world-state projection for the first deterministic milestone. */
public final class WorldModel {
    private final Map<String, WorldEntity> entities = new ConcurrentHashMap<>();

    public void upsert(WorldEntity entity) {
        entities.put(entity.id(), entity);
    }

    public Optional<WorldEntity> find(String id) {
        return Optional.ofNullable(entities.get(id));
    }

    public Collection<WorldEntity> snapshot() {
        return List.copyOf(entities.values());
    }

    public void clear() {
        entities.clear();
    }
}
