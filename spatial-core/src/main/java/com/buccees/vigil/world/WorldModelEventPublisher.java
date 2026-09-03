package com.buccees.vigil.world;

/** Receives world-model state-change events after successful mutation. */
@FunctionalInterface
public interface WorldModelEventPublisher {
    void publish(WorldModelEvent event);
}
