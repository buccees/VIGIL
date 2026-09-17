package com.buccees.vigil.priority;

import com.buccees.vigil.world.WorldEntity;
import com.buccees.vigil.world.WorldEntityFreshness;
import com.buccees.vigil.world.WorldEntityValidity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Deterministic, explainable consumer of authoritative World Entities. */
public final class RelevancePriorityEngine {
    public PriorityResult evaluate(WorldEntity entity, RelevancePriorityContext context, PriorityPolicy policy) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(policy, "policy");

        Map<PriorityResult.PriorityFactor, Double> factors = new EnumMap<>(PriorityResult.PriorityFactor.class);
        List<PriorityResult.PriorityFactor> unavailable = new ArrayList<>();

        if (context.referencePosition() == null) {
            unavailable.add(PriorityResult.PriorityFactor.PROXIMITY);
        } else {
            double distance = context.referencePosition().distanceTo(entity.position());
            factors.put(PriorityResult.PriorityFactor.PROXIMITY,
                    clamp01(1.0 - distance / policy.proximityRangeMeters()));
        }

        double speed = magnitude(entity.velocityMetersPerSecond());
        factors.put(PriorityResult.PriorityFactor.MOVEMENT,
                speed >= policy.movementThresholdMetersPerSecond() ? clamp01(speed / Math.max(policy.movementThresholdMetersPerSecond(), 1.0)) : 0.0);
        factors.put(PriorityResult.PriorityFactor.STATE_CHANGE,
                context.changedEntityIds().contains(entity.id()) ? 1.0 : 0.0);
        factors.put(PriorityResult.PriorityFactor.ZONE_RELATIONSHIP,
                context.relevantZoneEntityIds().contains(entity.id()) ? 1.0 : 0.0);

        if (context.activeTaskId() == null) {
            unavailable.add(PriorityResult.PriorityFactor.TASK_RELEVANCE);
        } else {
            factors.put(PriorityResult.PriorityFactor.TASK_RELEVANCE,
                    context.taskRelevantEntityIds().contains(entity.id()) ? 1.0 : 0.0);
        }

        factors.put(PriorityResult.PriorityFactor.UNEXPECTED_CHANGE,
                context.unexpectedEntityIds().contains(entity.id()) ? 1.0 : 0.0);
        factors.put(PriorityResult.PriorityFactor.PERSISTENCE,
                context.recurringEntityIds().contains(entity.id()) ? 1.0 : 0.0);

        double weighted = weightedAverage(factors, unavailable, policy);
        double freshnessMultiplier = freshnessMultiplier(entity.freshness(), policy);
        double validityMultiplier = entity.validity() == WorldEntityValidity.INVALID ? policy.invalidPriorityCeiling() : 1.0;
        double priority = clamp01(weighted * freshnessMultiplier * validityMultiplier);
        double relevance = clamp01(weighted);

        return new PriorityResult(entity.id(), relevance, priority, context.evaluationTime(), factors,
                java.util.Set.copyOf(unavailable),
                new PriorityResult.SourceStateSummary(entity.confidence().value(), entity.validity(), entity.freshness()));
    }

    public List<PriorityResult> order(List<WorldEntity> entities, RelevancePriorityContext context, PriorityPolicy policy) {
        Objects.requireNonNull(entities, "entities");
        return entities.stream()
                .map(entity -> evaluate(entity, context, policy))
                .sorted(Comparator.comparingDouble(PriorityResult::priority).reversed()
                        .thenComparing(PriorityResult::worldEntityId))
                .toList();
    }

    private static double weightedAverage(Map<PriorityResult.PriorityFactor, Double> factors,
                                          List<PriorityResult.PriorityFactor> unavailable,
                                          PriorityPolicy policy) {
        double total = 0.0;
        double weight = 0.0;
        for (PriorityResult.PriorityFactor factor : PriorityResult.PriorityFactor.values()) {
            if (unavailable.contains(factor)) continue;
            double factorWeight = weight(factor, policy);
            total += factors.get(factor) * factorWeight;
            weight += factorWeight;
        }
        return weight == 0.0 ? 0.0 : total / weight;
    }

    private static double weight(PriorityResult.PriorityFactor factor, PriorityPolicy policy) {
        return switch (factor) {
            case PROXIMITY -> policy.proximityWeight();
            case MOVEMENT -> policy.movementWeight();
            case STATE_CHANGE -> policy.stateChangeWeight();
            case ZONE_RELATIONSHIP -> policy.zoneWeight();
            case TASK_RELEVANCE -> policy.taskWeight();
            case UNEXPECTED_CHANGE -> policy.unexpectedWeight();
            case PERSISTENCE -> policy.persistenceWeight();
        };
    }

    private static double freshnessMultiplier(WorldEntityFreshness freshness, PriorityPolicy policy) {
        return switch (freshness) {
            case CURRENT -> 1.0;
            case AGING -> policy.agingMultiplier();
            case STALE -> policy.staleMultiplier();
        };
    }

    private static double magnitude(com.buccees.vigil.spatial.LocalPosition velocity) {
        return Math.sqrt(velocity.xM() * velocity.xM() + velocity.yM() * velocity.yM() + velocity.zM() * velocity.zM());
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
