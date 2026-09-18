package com.buccees.vigil.relevance;

import java.util.List;
import java.util.Objects;

/** Deterministic, explainable relevance assessment independent of confidence. */
public record RelevanceAssessment(
        double score,
        List<String> factors,
        String explanation
) {
    public RelevanceAssessment {
        if (!Double.isFinite(score) || score < 0.0 || score > 1.0) {
            throw new IllegalArgumentException("score must be between 0 and 1");
        }
        factors = List.copyOf(Objects.requireNonNull(factors, "factors"));
        if (explanation == null || explanation.isBlank()) {
            throw new IllegalArgumentException("explanation must not be blank");
        }
    }
}
