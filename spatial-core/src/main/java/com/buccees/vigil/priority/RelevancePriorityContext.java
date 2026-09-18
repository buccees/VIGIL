package com.buccees.vigil.priority;

import com.buccees.vigil.spatial.LocalPosition;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Explicit context supplied to deterministic relevance and priority evaluation. */
public record RelevancePriorityContext(
        LocalPosition referencePosition,
        String activeTaskId,
        Set<String> taskRelevantEntityIds,
        Set<String> changedEntityIds,
        Set<String> unexpectedEntityIds,
        Set<String> recurringEntityIds,
        Set<String> relevantZoneEntityIds,
        Instant evaluationTime
) {
    public RelevancePriorityContext {
        taskRelevantEntityIds = immutableSet(taskRelevantEntityIds, "taskRelevantEntityIds");
        changedEntityIds = immutableSet(changedEntityIds, "changedEntityIds");
        unexpectedEntityIds = immutableSet(unexpectedEntityIds, "unexpectedEntityIds");
        recurringEntityIds = immutableSet(recurringEntityIds, "recurringEntityIds");
        relevantZoneEntityIds = immutableSet(relevantZoneEntityIds, "relevantZoneEntityIds");
        Objects.requireNonNull(evaluationTime, "evaluationTime");
    }

    public static RelevancePriorityContext at(Instant evaluationTime) {
        return new RelevancePriorityContext(null, null, Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), evaluationTime);
    }

    private static Set<String> immutableSet(Set<String> value, String name) {
        Objects.requireNonNull(value, name);
        Set<String> copy = new HashSet<>(value);
        if (copy.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException(name + " must not contain null");
        return Collections.unmodifiableSet(copy);
    }
}
