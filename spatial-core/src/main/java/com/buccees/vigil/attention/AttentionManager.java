package com.buccees.vigil.attention;

import com.buccees.vigil.priority.PriorityResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AttentionManager {
    private final AttentionPolicy policy;
    private final Map<String, AttentionItem> items = new LinkedHashMap<>();

    public AttentionManager(AttentionPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public List<AttentionItem> update(List<PriorityResult> results, Instant now) {
        Objects.requireNonNull(results, "results");
        Objects.requireNonNull(now, "now");

        for (PriorityResult result : results) {
            Objects.requireNonNull(result, "results must not contain null");
            AttentionItem existing = items.get(result.worldEntityId());
            AttentionLifecycle lifecycle = existing == null ? AttentionLifecycle.NEW : existing.lifecycle();
            if (lifecycle == AttentionLifecycle.EXPIRED || lifecycle == AttentionLifecycle.DISMISSED) {
                lifecycle = AttentionLifecycle.NEW;
            }
            items.put(result.worldEntityId(), AttentionItem.from(result).withLifecycle(lifecycle));
        }

        expire(now);

        List<AttentionItem> ordered = items.values().stream()
                .filter(item -> item.lifecycle() != AttentionLifecycle.EXPIRED && item.lifecycle() != AttentionLifecycle.DISMISSED)
                .filter(item -> item.priority() >= policy.minimumPriority())
                .sorted(Comparator.comparingDouble(AttentionItem::priority).reversed()
                        .thenComparing(Comparator.comparingDouble(AttentionItem::relevance).reversed())
                        .thenComparing(AttentionItem::worldEntityId))
                .toList();

        List<AttentionItem> active = new ArrayList<>();
        for (AttentionItem item : ordered) {
            if (active.size() >= policy.maximumActiveItems()) break;
            AttentionLifecycle lifecycle = item.lifecycle() == AttentionLifecycle.ACKNOWLEDGED
                    ? AttentionLifecycle.ACKNOWLEDGED : AttentionLifecycle.ACTIVE;
            AttentionItem updated = item.withLifecycle(lifecycle);
            items.put(updated.worldEntityId(), updated);
            active.add(updated);
        }
        return List.copyOf(active);
    }

    public AttentionItem acknowledge(String worldEntityId) {
        return transition(worldEntityId, AttentionLifecycle.ACKNOWLEDGED);
    }

    public AttentionItem dismiss(String worldEntityId) {
        return transition(worldEntityId, AttentionLifecycle.DISMISSED);
    }

    public List<AttentionItem> snapshot() {
        return List.copyOf(items.values());
    }

    private AttentionItem transition(String worldEntityId, AttentionLifecycle lifecycle) {
        AttentionItem current = items.get(worldEntityId);
        if (current == null) throw new IllegalArgumentException("Unknown attention item: " + worldEntityId);
        AttentionItem updated = current.withLifecycle(lifecycle);
        items.put(worldEntityId, updated);
        return updated;
    }

    private void expire(Instant now) {
        for (Map.Entry<String, AttentionItem> entry : items.entrySet()) {
            AttentionItem item = entry.getValue();
            if (item.lifecycle() == AttentionLifecycle.ACKNOWLEDGED || item.lifecycle() == AttentionLifecycle.DISMISSED)
                continue;
            if (policy.persistenceMillis() == 0 ||
                    !item.evaluationTime().plusMillis(policy.persistenceMillis()).isAfter(now)) {
                entry.setValue(item.withLifecycle(AttentionLifecycle.EXPIRED));
            }
        }
    }
}
