package com.buccees.vigil.interaction;

import com.buccees.vigil.attention.AttentionItem;
import com.buccees.vigil.priority.PriorityResult;
import com.buccees.vigil.world.WorldEntityFreshness;
import com.buccees.vigil.world.WorldEntityValidity;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class AttentionInteractionRouterTest {
    private static final Instant NOW = Instant.parse("2026-09-18T12:00:00Z");

    @Test
    void handoffPreservesEntityIdentityAndPriority() {
        AttentionItem item = new AttentionItem("entity-7", .8, .9, NOW, .7,
                WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT, Map.of(), Set.of(),
                com.buccees.vigil.attention.AttentionLifecycle.ACTIVE);
        AttentionInteractionHandoff handoff = AttentionInteractionHandoff.from("req-1", "session-1", item, NOW);
        assertEquals("entity-7", handoff.worldEntityId());
        assertEquals(.9, handoff.priority());
        assertEquals(.8, handoff.relevance());
    }

    @Test
    void routerProducesStructuredRequestWithoutGrantingOperationPermission() {
        AttentionItem item = new AttentionItem("entity-7", .8, .9, NOW, .7,
                WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT, Map.of(), Set.of(),
                com.buccees.vigil.attention.AttentionLifecycle.ACTIVE);
        InteractionSession session = InteractionSession.create("session-1", NOW).activate(NOW.plusSeconds(1));
        AttentionInteractionHandoff handoff = AttentionInteractionHandoff.from("req-1", "session-1", item, NOW.plusSeconds(2));

        HumanInteractionRequest request = new AttentionInteractionRouter().createRequest(
                handoff, session, InputModality.TEXT, AuthenticationState.AUTHENTICATED,
                new AuthorizationContext(Set.of()), NOW.plusSeconds(3));

        assertEquals("entity-7", request.requestedScope());
        assertEquals(InputModality.TEXT, request.modality());
        assertTrue(request.authenticated());
        assertTrue(request.authorized());
        assertEquals(null, request.requestedOperation());
        assertEquals("attention-handoff:entity-7", request.interpretationProvenance());
    }

    @Test
    void handoffDoesNotInventAuthentication() {
        AttentionItem item = new AttentionItem("entity-7", .8, .9, NOW, .7,
                WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT, Map.of(), Set.of(),
                com.buccees.vigil.attention.AttentionLifecycle.ACTIVE);
        InteractionSession session = InteractionSession.create("session-1", NOW).activate(NOW.plusSeconds(1));
        AttentionInteractionHandoff handoff = AttentionInteractionHandoff.from("req-2", "session-1", item, NOW.plusSeconds(2));

        HumanInteractionRequest request = new AttentionInteractionRouter().createRequest(
                handoff, session, InputModality.TEXT, AuthenticationState.UNAUTHENTICATED,
                new AuthorizationContext(Set.of()), NOW.plusSeconds(3));

        assertFalse(request.authenticated());
    }

    @Test
    void inactiveSessionCannotReceiveAttentionHandoff() {
        AttentionItem item = new AttentionItem("entity-7", .8, .9, NOW, .7,
                WorldEntityValidity.VALID, WorldEntityFreshness.CURRENT, Map.of(), Set.of(),
                com.buccees.vigil.attention.AttentionLifecycle.ACTIVE);
        InteractionSession session = InteractionSession.create("session-1", NOW);
        AttentionInteractionHandoff handoff = AttentionInteractionHandoff.from("req-1", "session-1", item, NOW);
        assertThrows(IllegalStateException.class, () ->
                new AttentionInteractionRouter().createRequest(handoff, session, InputModality.TEXT,
                        AuthenticationState.AUTHENTICATED, new AuthorizationContext(Set.of()), NOW));
    }
}
