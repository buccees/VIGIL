package com.buccees.vigil.relevance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AttentionQueue {
    private final Map<String, AttentionQueueEntry> entries = new LinkedHashMap<>();

    public void offer(AttentionQueueEntry entry) {
        entries.put(entry.entityId(), entry);
    }

    public List<AttentionQueueEntry> snapshot() {
        return entries.values().stream()
                .sorted(Comparator.comparingDouble((AttentionQueueEntry e) -> e.decision().priority()).reversed()
                        .thenComparing(AttentionQueueEntry::entityId))
                .toList();
    }

    public void clear() {
        entries.clear();
    }
}
