package com.buccees.vigil.attention;

import com.buccees.vigil.priority.PriorityResult;
import com.buccees.vigil.world.WorldEntityFreshness;
import com.buccees.vigil.world.WorldEntityValidity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AttentionManagerTest {
    private static final Instant T0 = Instant.parse("2026-09-18T12:00:00Z");

    @Test
    void orderingIsDeterministicAndUsesStableTieBreaks() {
        AttentionManager manager = new AttentionManager(new AttentionPolicy(0.0, 5, 10_000));
        PriorityResult b = result("b", .8, .7);
        PriorityResult a = result("a", .8, .7);

        List<AttentionItem> ordered = manager.update(List.of(b, a), T0);

        assertEquals(List.of("a", "b"), ordered.stream().map(AttentionItem::worldEntityId).toList());
    }

    @Test
    void repeatedEvaluationUpdatesInsteadOfDuplicating() {
        AttentionManager manager = new AttentionManager(new AttentionPolicy(0.0, 5, 10_000));

        manager.update(List.of(result("entity-1", .5, .4)), T0);
        manager.update(List.of(result("entity-1", .9, .9)), T0.plusSeconds(1));

        assertEquals(1, manager.snapshot().size());
        assertEquals(.9, manager.snapshot().get(0).priority());
    }

    @Test
    void lifecycleTransitionsDoNotChangeSourceFields() {
        AttentionManager manager = new AttentionManager(new AttentionPolicy(0.0, 5, 10_000));
        AttentionItem item = manager.update(List.of(result("entity-1", .7, .8)), T0).get(0);

        AttentionItem acknowledged = manager.acknowledge(item.worldEntityId());
        assertEquals(AttentionLifecycle.ACKNOWLEDGED, acknowledged.lifecycle());
        assertEquals(item.sourceConfidence(), acknowledged.sourceConfidence());
        assertEquals(item.sourceValidity(), acknowledged.sourceValidity());
        assertEquals(item.sourceFreshness(), acknowledged.sourceFreshness());

        AttentionItem dismissed = manager.dismiss(item.worldEntityId());
        assertEquals(AttentionLifecycle.DISMISSED, dismissed.lifecycle());
        assertEquals(item.worldEntityId(), dismissed.worldEntityId());
    }

    @Test
    void expirationChangesPresentationStateOnly() {
        AttentionManager manager = new AttentionManager(new AttentionPolicy(0.0, 5, 1_000));
        manager.update(List.of(result("entity-1", .7, .8)), T0);

        manager.update(List.of(), T0.plusSeconds(2));

        assertEquals(AttentionLifecycle.EXPIRED, manager.snapshot().get(0).lifecycle());
        assertEquals("entity-1", manager.snapshot().get(0).worldEntityId());
    }

    @Test
    void lowPriorityIsRetainedInManagerSnapshot() {
        AttentionManager manager = new AttentionManager(new AttentionPolicy(.9, 1, 10_000));
        manager.update(List.of(result("low", .2, .1)), T0);

        assertEquals(1, manager.snapshot().size());
        assertEquals("low", manager.snapshot().get(0).worldEntityId());
        assertTrue(manager.update(List.of(), T0).isEmpty());
    }

    private static PriorityResult result(String id, double relevance, double priority) {
        return new PriorityResult(id, relevance, priority, T0, Map.of(),
                Set.of(), new PriorityResult.SourceStateSummary(.8, WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT));
    }
}
