package com.buccees.vigil.relevance;

import java.util.List;

public final class PresentationDispatcher {
    public List<PresentationDispatchRequest> select(AttentionQueue queue) {
        return queue.snapshot().stream()
                .filter(entry -> entry.decision().action() == PresentationAction.PRESENT)
                .map(entry -> new PresentationDispatchRequest(
                        entry.entityId(),
                        entry.decision().explanation()))
                .toList();
    }
}
