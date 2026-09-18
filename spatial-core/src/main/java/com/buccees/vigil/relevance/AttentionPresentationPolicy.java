package com.buccees.vigil.relevance;

import java.util.Objects;

public final class AttentionPresentationPolicy {
    private final double presentThreshold;
    private final double deferThreshold;

    public AttentionPresentationPolicy(double presentThreshold, double deferThreshold) {
        if (!Double.isFinite(presentThreshold) || !Double.isFinite(deferThreshold)
                || presentThreshold < 0 || presentThreshold > 1
                || deferThreshold < 0 || deferThreshold > 1
                || deferThreshold > presentThreshold) {
            throw new IllegalArgumentException("thresholds must be between 0 and 1 with deferThreshold <= presentThreshold");
        }
        this.presentThreshold = presentThreshold;
        this.deferThreshold = deferThreshold;
    }

    public PresentationDecision decide(String entityId, PriorityAssessment priority) {
        Objects.requireNonNull(priority, "priority");
        double score = priority.score();
        PresentationAction action = score >= presentThreshold
                ? PresentationAction.PRESENT
                : score >= deferThreshold
                ? PresentationAction.DEFER
                : PresentationAction.SUPPRESS;
        return new PresentationDecision(entityId, action, score,
                "Presentation is selected from explicit priority thresholds; the policy does not mutate world state or execute operations.");
    }
}
