package com.buccees.vigil.priority;

import com.buccees.vigil.world.WorldEntityFreshness;
import com.buccees.vigil.world.WorldEntityValidity;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Immutable derived result; it never becomes authoritative world state. */
public record PriorityResult(
        String worldEntityId,
        double relevance,
        double priority,
        Instant evaluationTime,
        Map<PriorityFactor, Double> contributingFactors,
        Set<PriorityFactor> unavailableFactors,
        SourceStateSummary sourceStateSummary
) {
    public PriorityResult {
        if (worldEntityId == null || worldEntityId.isBlank()) throw new IllegalArgumentException("worldEntityId must not be blank");
        requireUnit(relevance, "relevance");
        requireUnit(priority, "priority");
        Objects.requireNonNull(evaluationTime, "evaluationTime");
        Objects.requireNonNull(contributingFactors, "contributingFactors");
        Objects.requireNonNull(unavailableFactors, "unavailableFactors");
        Objects.requireNonNull(sourceStateSummary, "sourceStateSummary");
        EnumMap<PriorityFactor, Double> factorCopy = new EnumMap<>(PriorityFactor.class);
        factorCopy.putAll(contributingFactors);
        contributingFactors = Collections.unmodifiableMap(factorCopy);
        EnumSet<PriorityFactor> unavailableCopy = EnumSet.noneOf(PriorityFactor.class);
        unavailableCopy.addAll(unavailableFactors);
        unavailableFactors = Collections.unmodifiableSet(unavailableCopy);
    }

    private static void requireUnit(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) throw new IllegalArgumentException(name + " must be between 0 and 1");
    }

    public enum PriorityFactor {
        PROXIMITY,
        MOVEMENT,
        STATE_CHANGE,
        ZONE_RELATIONSHIP,
        TASK_RELEVANCE,
        UNEXPECTED_CHANGE,
        PERSISTENCE
    }

    public record SourceStateSummary(
            double confidence,
            WorldEntityValidity validity,
            WorldEntityFreshness freshness
    ) {
        public SourceStateSummary {
            Objects.requireNonNull(validity, "validity");
            Objects.requireNonNull(freshness, "freshness");
        }
    }
}
