package com.buccees.vigil.attention;

import com.buccees.vigil.priority.PriorityResult;
import com.buccees.vigil.world.WorldEntityFreshness;
import com.buccees.vigil.world.WorldEntityValidity;

import java.time.Instant;
import java.util.Objects;

public record AttentionItem(
        String worldEntityId,
        double relevance,
        double priority,
        Instant evaluationTime,
        double sourceConfidence,
        WorldEntityValidity sourceValidity,
        WorldEntityFreshness sourceFreshness,
        java.util.Map<PriorityResult.PriorityFactor, Double> contributingFactors,
        java.util.Set<PriorityResult.PriorityFactor> unavailableFactors,
        AttentionLifecycle lifecycle
) {
    public AttentionItem {
        if (worldEntityId == null || worldEntityId.isBlank()) throw new IllegalArgumentException("worldEntityId must not be blank");
        Objects.requireNonNull(evaluationTime, "evaluationTime");
        Objects.requireNonNull(sourceValidity, "sourceValidity");
        Objects.requireNonNull(sourceFreshness, "sourceFreshness");
        Objects.requireNonNull(contributingFactors, "contributingFactors");
        Objects.requireNonNull(unavailableFactors, "unavailableFactors");
        Objects.requireNonNull(lifecycle, "lifecycle");
        contributingFactors = java.util.Map.copyOf(contributingFactors);
        unavailableFactors = java.util.Set.copyOf(unavailableFactors);
    }

    public static AttentionItem from(PriorityResult result) {
        return new AttentionItem(
                result.worldEntityId(),
                result.relevance(),
                result.priority(),
                result.evaluationTime(),
                result.sourceStateSummary().confidence(),
                result.sourceStateSummary().validity(),
                result.sourceStateSummary().freshness(),
                result.contributingFactors(),
                result.unavailableFactors(),
                AttentionLifecycle.NEW);
    }

    public AttentionItem withLifecycle(AttentionLifecycle next) {
        return new AttentionItem(worldEntityId, relevance, priority, evaluationTime, sourceConfidence,
                sourceValidity, sourceFreshness, contributingFactors, unavailableFactors, next);
    }
}
