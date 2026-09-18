package com.buccees.vigil.relevance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class AttentionQueueTest {
    private PresentationDecision decision(String id, double priority) {
        return new PresentationDecision(id, PresentationAction.PRESENT, priority, "test");
    }

    @Test
    void duplicateEntityReplacesOlderEntry() {
        AttentionQueue queue = new AttentionQueue();
        queue.offer(new AttentionQueueEntry("entity-1", decision("entity-1", .4)));
        queue.offer(new AttentionQueueEntry("entity-1", decision("entity-1", .9)));

        assertEquals(1, queue.snapshot().size());
        assertEquals(.9, queue.snapshot().get(0).decision().priority());
    }

    @Test
    void snapshotIsDeterministicallyOrderedByPriorityThenEntityId() {
        AttentionQueue queue = new AttentionQueue();
        queue.offer(new AttentionQueueEntry("entity-b", decision("entity-b", .8)));
        queue.offer(new AttentionQueueEntry("entity-a", decision("entity-a", .8)));
        queue.offer(new AttentionQueueEntry("entity-c", decision("entity-c", .2)));

        assertEquals(
                List.of("entity-a", "entity-b", "entity-c"),
                queue.snapshot().stream().map(AttentionQueueEntry::entityId).toList());
    }

    @Test
    void clearRemovesPendingPresentationEntries() {
        AttentionQueue queue = new AttentionQueue();
        queue.offer(new AttentionQueueEntry("entity-1", decision("entity-1", .8)));
        queue.clear();
        assertTrue(queue.snapshot().isEmpty());
    }
}
