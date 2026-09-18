package com.buccees.vigil.relevance;

import static org.junit.jupiter.api.Assertions.*;

import com.buccees.vigil.spatial.LocalPosition;
import com.buccees.vigil.world.Confidence;
import com.buccees.vigil.world.EntityType;
import com.buccees.vigil.world.TrackLifecycleState;
import com.buccees.vigil.world.WorldEntity;
import com.buccees.vigil.world.WorldEntityFreshness;
import com.buccees.vigil.world.WorldEntityValidity;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RelevancePriorityEngineTest {
    private static final Instant NOW = Instant.parse("2026-09-18T15:00:00Z");

    @Test
    void relevanceIsDrivenByContextAndFreshnessNotConfidence() {
        RelevancePriorityEngine engine = new RelevancePriorityEngine(100.0, Duration.ofMinutes(1));
        RelevanceContext context = new RelevanceContext(new LocalPosition(0, 0, 0), "inspect nearby entities");

        WorldEntity lowConfidence = entity("a", new LocalPosition(10, 0, 0), new LocalPosition(0, 0, 0), 0.1);
        WorldEntity highConfidence = entity("b", new LocalPosition(10, 0, 0), new LocalPosition(0, 0, 0), 0.9);

        assertEquals(
                engine.assessRelevance(lowConfidence, context, NOW).score(),
                engine.assessRelevance(highConfidence, context, NOW).score(),
                0.000001);
    }

    @Test
    void relevanceDropsOutsideConfiguredRadius() {
        RelevancePriorityEngine engine = new RelevancePriorityEngine(100.0, Duration.ofMinutes(1));
        RelevanceContext context = new RelevanceContext(new LocalPosition(0, 0, 0), "inspect");

        var nearby = engine.assessRelevance(entity("near", new LocalPosition(10, 0, 0), new LocalPosition(0, 0, 0), 0.5), context, NOW);
        var distant = engine.assessRelevance(entity("far", new LocalPosition(200, 0, 0), new LocalPosition(0, 0, 0), 0.5), context, NOW);

        assertTrue(nearby.score() > distant.score());
        assertTrue(nearby.factors().contains("proximity"));
        assertFalse(distant.factors().contains("proximity"));
    }

    @Test
    void priorityAddsExplicitMovementFactorWithoutChangingWorldState() {
        RelevancePriorityEngine engine = new RelevancePriorityEngine(100.0, Duration.ofMinutes(1));
        RelevanceContext context = new RelevanceContext(new LocalPosition(0, 0, 0), "inspect");
        WorldEntity moving = entity("moving", new LocalPosition(10, 0, 0), new LocalPosition(1, 0, 0), 0.5);

        var before = moving;
        var relevance = engine.assessRelevance(moving, context, NOW);
        var priority = engine.assessPriority(moving, relevance, NOW);

        assertTrue(priority.factors().contains("movement"));
        assertEquals(before, moving);
    }

    private static WorldEntity entity(String id, LocalPosition position, LocalPosition velocity, double confidence) {
        return new WorldEntity(
                id, EntityType.UNKNOWN, position, velocity, new Confidence(confidence), NOW,
                "track-" + id, List.of("track-" + id), List.of("det-" + id),
                TrackLifecycleState.CONFIRMED, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
    }
}
