package com.buccees.vigil.interaction;

import java.time.Instant;
import java.util.Objects;

public record InteractionSession(
        String sessionId,
        InteractionSessionState state,
        Instant createdAt,
        Instant lastActivityAt
) {
    public InteractionSession {
        if (sessionId == null || sessionId.isBlank()) throw new IllegalArgumentException("sessionId must not be blank");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(lastActivityAt, "lastActivityAt");
        if (lastActivityAt.isBefore(createdAt)) throw new IllegalArgumentException("lastActivityAt must not precede createdAt");
    }

    public static InteractionSession create(String sessionId, Instant now) {
        Objects.requireNonNull(now, "now");
        return new InteractionSession(sessionId, InteractionSessionState.CREATED, now, now);
    }

    public InteractionSession activate(Instant now) {
        Objects.requireNonNull(now, "now");
        requireTransition(InteractionSessionState.CREATED, InteractionSessionState.ACTIVE);
        return new InteractionSession(sessionId, InteractionSessionState.ACTIVE, createdAt, now);
    }

    public InteractionSession touch(Instant now) {
        Objects.requireNonNull(now, "now");
        if (state != InteractionSessionState.ACTIVE) {
            throw new IllegalStateException("Only active sessions may receive interaction");
        }
        if (now.isBefore(lastActivityAt)) throw new IllegalArgumentException("now must not precede lastActivityAt");
        return new InteractionSession(sessionId, state, createdAt, now);
    }

    public InteractionSession close(Instant now) {
        Objects.requireNonNull(now, "now");
        if (state != InteractionSessionState.ACTIVE && state != InteractionSessionState.CREATED) {
            throw new IllegalStateException("Only created or active sessions may close");
        }
        if (now.isBefore(lastActivityAt)) throw new IllegalArgumentException("now must not precede lastActivityAt");
        return new InteractionSession(sessionId, InteractionSessionState.CLOSED, createdAt, now);
    }

    public InteractionSession expire(Instant now) {
        Objects.requireNonNull(now, "now");
        if (state != InteractionSessionState.ACTIVE) {
            throw new IllegalStateException("Only active sessions may expire");
        }
        if (now.isBefore(lastActivityAt)) throw new IllegalArgumentException("now must not precede lastActivityAt");
        return new InteractionSession(sessionId, InteractionSessionState.EXPIRED, createdAt, now);
    }

    public boolean acceptsInteraction() {
        return state == InteractionSessionState.ACTIVE;
    }

    private void requireTransition(InteractionSessionState expected, InteractionSessionState target) {
        if (state != expected) {
            throw new IllegalStateException("Cannot transition " + state + " to " + target);
        }
    }
}
