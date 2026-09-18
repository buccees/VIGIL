package com.buccees.vigil.relevance;

import com.buccees.vigil.world.WorldEntity;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Deterministic relevance and priority calculation over authoritative world information.
 * Confidence is deliberately not used as a proxy for relevance or priority.
 */
public final class RelevancePriorityEngine {
    private final double relevanceRadiusMeters;
    private final Duration maxFreshness;

    public RelevancePriorityEngine(double relevanceRadiusMeters, Duration maxFreshness) {
        if (!Double.isFinite(relevanceRadiusMeters) || relevanceRadiusMeters <= 0.0) {
            throw new IllegalArgumentException("relevanceRadiusMeters must be positive");
        }
        this.relevanceRadiusMeters = relevanceRadiusMeters;
        this.maxFreshness = Objects.requireNonNull(maxFreshness, "maxFreshness");
        if (maxFreshness.isNegative()) throw new IllegalArgumentException("maxFreshness must not be negative");
    }

    public RelevanceAssessment assessRelevance(WorldEntity entity, RelevanceContext context, Instant now) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(now, "now");

        double distance = context.userPosition().distanceTo(entity.position());
        double proximity = Math.max(0.0, 1.0 - (distance / relevanceRadiusMeters));
        boolean fresh = !entity.lastUpdated().isAfter(now)
                && !entity.lastUpdated().plus(maxFreshness).isBefore(now);

        List<String> factors = new ArrayList<>();
        if (proximity > 0.0) factors.add("proximity");
        if (fresh) factors.add("freshness");
        if (!context.task().isBlank()) factors.add("active-task-context");

        double score = proximity * 0.7 + (fresh ? 0.2 : 0.0) + 0.1;
        score = clamp(score);

        return new RelevanceAssessment(score, factors,
                "Relevance is based on spatial proximity, freshness, and the presence of an explicit user task; confidence is not a relevance factor.");
    }

    public PriorityAssessment assessPriority(WorldEntity entity, RelevanceAssessment relevance, Instant now) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(relevance, "relevance");
        Objects.requireNonNull(now, "now");

        boolean moving = entity.velocityMetersPerSecond().distanceTo(
                new com.buccees.vigil.spatial.LocalPosition(0.0, 0.0, 0.0)) > 0.1;
        boolean fresh = !entity.lastUpdated().isAfter(now)
                && !entity.lastUpdated().plus(maxFreshness).isBefore(now);

        List<String> factors = new ArrayList<>();
        factors.add("relevance");
        if (moving) factors.add("movement");
        if (fresh) factors.add("freshness");

        double score = relevance.score() * 0.75 + (moving ? 0.15 : 0.0) + (fresh ? 0.10 : 0.0);
        return new PriorityAssessment(clamp(score), factors,
                "Priority combines relevance with explicit movement and freshness factors; it does not alter world state.");
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
