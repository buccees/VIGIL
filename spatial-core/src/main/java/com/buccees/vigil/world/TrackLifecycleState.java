package com.buccees.vigil.world;

/** Lifecycle of a temporally maintained track. */
public enum TrackLifecycleState {
    TENTATIVE,
    CONFIRMED,
    DEGRADED,
    STALE,
    TERMINATED
}
