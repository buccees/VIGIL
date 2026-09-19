package com.buccees.vigil.priority;

import com.buccees.vigil.spatial.LocalPosition;
import com.buccees.vigil.world.Confidence;
import com.buccees.vigil.world.EntityType;
import com.buccees.vigil.world.TrackLifecycleState;
import com.buccees.vigil.world.WorldEntity;
import com.buccees.vigil.world.WorldEntityFreshness;
import com.buccees.vigil.world.WorldEntityValidity;
import com.buccees.vigil.world.WorldModel;
import com.buccees.vigil.world.WorldModelUpdater;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelevancePriorityEngineTest {
    private static final Instant T0 = Instant.parse("2026-09-17T20:00:00Z");
    private final RelevancePriorityEngine engine = new RelevancePriorityEngine();

    @Test
    void identicalInputsProduceIdenticalResults() {
        WorldEntity entity = entity("entity-1", 10, 0, 0, 0, 0.9, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
        RelevancePriorityContext context = new RelevancePriorityContext(
                new LocalPosition(0, 0, 0), "task-1", Set.of("entity-1"), Set.of(), Set.of(), Set.of(), Set.of(), T0);

        assertEquals(engine.evaluate(entity, context, PriorityPolicy.defaults()),
                engine.evaluate(entity, context, PriorityPolicy.defaults()));
    }

    @Test
    void proximityChangesPriorityWithoutChangingConfidence() {
        PriorityPolicy policy = new PriorityPolicy(1.0, 0, 0, 0, 0, 0, 0, 100, 1, 1, 1, 0.2);
        WorldEntity near = entity("near", 10, 0, 0, 0, 0.2, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
        WorldEntity far = entity("far", 90, 0, 0, 0, 0.2, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
        RelevancePriorityContext context = new RelevancePriorityContext(new LocalPosition(0, 0, 0), null,
                Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), T0);

        PriorityResult nearResult = engine.evaluate(near, context, policy);
        PriorityResult farResult = engine.evaluate(far, context, policy);

        assertTrue(nearResult.priority() > farResult.priority());
        assertEquals(near.confidence(), far.confidence());
        assertEquals(0.2, nearResult.sourceStateSummary().confidence());
    }

    @Test
    void stateChangeRaisesPriorityWithoutMutatingWorldEntity() {
        WorldEntity entity = entity("entity-1", 10, 0, 0, 0, 0.8, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
        WorldEntity before = entity;
        RelevancePriorityContext context = new RelevancePriorityContext(null, null, Set.of(), Set.of("entity-1"),
                Set.of(), Set.of(), Set.of(), T0);

        PriorityResult result = engine.evaluate(entity, context, PriorityPolicy.defaults());

        assertEquals(before, entity);
        assertEquals(1.0, result.contributingFactors().get(PriorityResult.PriorityFactor.STATE_CHANGE));
    }

    @Test
    void taskRelevanceIsIndependentFromConfidence() {
        PriorityPolicy policy = new PriorityPolicy(0, 0, 0, 0, 1, 0, 0, 100, 1, 1, 1, 0.2);
        WorldEntity lowConfidence = entity("entity-1", 10, 0, 0, 0, 0.1, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
        WorldEntity highConfidence = entity("entity-2", 10, 0, 0, 0, 0.9, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
        RelevancePriorityContext context = new RelevancePriorityContext(null, "task-1", Set.of("entity-1"), Set.of(),
                Set.of(), Set.of(), Set.of(), T0);

        PriorityResult result = engine.evaluate(lowConfidence, context, policy);

        assertEquals(1.0, result.priority());
        assertEquals(0.1, result.sourceStateSummary().confidence());
        assertEquals(0.9, highConfidence.confidence().value());
    }

    @Test
    void invalidAndStaleStateRemainVisibleInResult() {
        PriorityPolicy policy = new PriorityPolicy(1, 0, 0, 0, 0, 0, 0, 100, 1, 0.25, 0.2, 0.0);
        WorldEntity entity = entity("entity-1", 0, 0, 0, 0, 0.9, WorldEntityValidity.INVALID, WorldEntityFreshness.STALE);
        RelevancePriorityContext context = RelevancePriorityContext.at(T0);

        PriorityResult result = engine.evaluate(entity, context, policy);

        assertEquals(WorldEntityValidity.INVALID, result.sourceStateSummary().validity());
        assertEquals(WorldEntityFreshness.STALE, result.sourceStateSummary().freshness());
        assertEquals(0.0, result.priority());
    }

    @Test
    void unavailableContextIsExplicitAndDeterministic() {
        RelevancePriorityContext context = RelevancePriorityContext.at(T0);
        WorldEntity entity = entity("entity-1", 10, 0, 0, 0, 0.9, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);

        PriorityResult first = engine.evaluate(entity, context, PriorityPolicy.defaults());
        PriorityResult second = engine.evaluate(entity, context, PriorityPolicy.defaults());

        assertTrue(first.unavailableFactors().contains(PriorityResult.PriorityFactor.PROXIMITY));
        assertTrue(first.unavailableFactors().contains(PriorityResult.PriorityFactor.TASK_RELEVANCE));
        assertEquals(first, second);
    }

    @Test
    void equalPriorityUsesWorldEntityIdAsStableTieBreaker() {
        PriorityPolicy policy = new PriorityPolicy(0, 0, 0, 0, 0, 0, 1, 100, 1, 1, 1, 0.2);
        WorldEntity a = entity("entity-a", 10, 0, 0, 0, 0.9, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
        WorldEntity b = entity("entity-b", 90, 0, 0, 0, 0.1, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
        RelevancePriorityContext context = new RelevancePriorityContext(null, null, Set.of(), Set.of(), Set.of(),
                Set.of("entity-a", "entity-b"), Set.of(), T0);

        assertEquals(List.of("entity-a", "entity-b"), engine.order(List.of(b, a), context, policy).stream()
                .map(PriorityResult::worldEntityId).toList());
    }

    @Test
    void lowPriorityDoesNotRemoveWorldEntity() {
        WorldModel model = new WorldModel();
        WorldModelUpdater updater = new WorldModelUpdater(model);
        WorldEntity entity = entity("entity-1", 1000, 0, 0, 0, 0.9, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT);
        updater.update(new com.buccees.vigil.world.Track(
                "track-entity-1", EntityType.PERSON, entity.position(), entity.velocityMetersPerSecond(),
                entity.confidence(), entity.lastUpdated(), List.of("detection-entity-1"), TrackLifecycleState.CONFIRMED));
        RelevancePriorityContext context = RelevancePriorityContext.at(T0);

        engine.evaluate(entity, context, PriorityPolicy.defaults());

        assertTrue(model.find("entity-1").isPresent());
        assertEquals(entity, model.find("entity-1").orElseThrow());
    }

    private static WorldEntity entity(String id, double x, double y, double z, double vx, double confidence,
                                      WorldEntityValidity validity, WorldEntityFreshness freshness) {
        return new WorldEntity(id, EntityType.PERSON, new LocalPosition(x, y, z), new LocalPosition(vx, 0, 0),
                new Confidence(confidence), T0, "track-" + id, List.of("detection-" + id),
                TrackLifecycleState.CONFIRMED, validity, freshness);
    }
}
