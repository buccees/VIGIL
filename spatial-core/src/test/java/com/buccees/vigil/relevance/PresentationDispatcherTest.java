package com.buccees.vigil.relevance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class PresentationDispatcherTest {
    private PresentationDecision decision(String id, PresentationAction action, double priority) {
        return new PresentationDecision(id, action, priority, "because-" + id);
    }

    @Test
    void selectsOnlyPresentDecisionsInQueueOrder() {
        AttentionQueue queue = new AttentionQueue();
        queue.offer(new AttentionQueueEntry("entity-b", decision("entity-b", PresentationAction.DEFER, .9)));
        queue.offer(new AttentionQueueEntry("entity-a", decision("entity-a", PresentationAction.PRESENT, .8)));
        queue.offer(new AttentionQueueEntry("entity-c", decision("entity-c", PresentationAction.PRESENT, .8)));
        queue.offer(new AttentionQueueEntry("entity-d", decision("entity-d", PresentationAction.SUPPRESS, 1.0)));

        List<PresentationDispatchRequest> selected = new PresentationDispatcher().select(queue);

        assertEquals(
                List.of("entity-a", "entity-c"),
                selected.stream().map(PresentationDispatchRequest::entityId).toList());
    }

    @Test
    void selectionDoesNotRemoveQueueEntries() {
        AttentionQueue queue = new AttentionQueue();
        queue.offer(new AttentionQueueEntry("entity-a", decision("entity-a", PresentationAction.PRESENT, .8)));

        PresentationDispatcher dispatcher = new PresentationDispatcher();
        dispatcher.select(queue);

        assertEquals(1, queue.snapshot().size());
    }

    @Test
    void requestRejectsBlankIdentityAndExplanation() {
        assertThrows(IllegalArgumentException.class,
                () -> new PresentationDispatchRequest(" ", "because"));
        assertThrows(IllegalArgumentException.class,
                () -> new PresentationDispatchRequest("entity-a", " "));
    }
}
