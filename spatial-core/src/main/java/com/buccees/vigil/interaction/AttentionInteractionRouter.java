package com.buccees.vigil.interaction;

import com.buccees.vigil.attention.AttentionItem;
import java.time.Instant;
import java.util.Objects;

public final class AttentionInteractionRouter {
    public HumanInteractionRequest createRequest(
            AttentionInteractionHandoff handoff,
            InteractionSession session,
            InputModality modality,
            AuthenticationState authentication,
            AuthorizationContext authorization,
            Instant now
    ) {
        Objects.requireNonNull(handoff, "handoff");
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(modality, "modality");
        Objects.requireNonNull(authentication, "authentication");
        Objects.requireNonNull(authorization, "authorization");
        Objects.requireNonNull(now, "now");

        if (!session.acceptsInteraction()) throw new IllegalStateException("Session is not active");
        if (!session.sessionId().equals(handoff.sessionId())) throw new IllegalArgumentException("Session mismatch");

        return new HumanInteractionRequest(
                handoff.requestId(),
                session.sessionId(),
                authentication,
                authorization,
                modality,
                handoff.prompt(),
                null,
                handoff.worldEntityId(),
                now,
                "attention-handoff:" + handoff.worldEntityId());
    }
}
