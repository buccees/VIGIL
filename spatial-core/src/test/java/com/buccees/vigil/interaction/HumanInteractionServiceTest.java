package com.buccees.vigil.interaction;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class HumanInteractionServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-18T12:00:00Z");

    @Test
    void textAndSpeechShareStructuredRequestModel() {
        HumanInteractionRequest text = request("r1", InputModality.TEXT, null, Set.of());
        HumanInteractionRequest speech = request("r2", InputModality.SPEECH, null, Set.of());
        assertEquals("r1", text.requestId());
        assertEquals(InputModality.SPEECH, speech.modality());
        assertEquals("speech-recognition-v1", speech.interpretationProvenance());
    }

    @Test
    void authenticationAndAuthorizationRemainDistinct() {
        HumanInteractionRequest unauth = request("r1", InputModality.TEXT, "VIEW", Set.of("VIEW"));
        HumanInteractionResponse response = new HumanInteractionService().authorize(unauth, NOW);
        assertEquals(HumanInteractionResponse.ResponseStatus.AUTHENTICATION_REQUIRED, response.status());

        HumanInteractionRequest forbidden = requestAuthenticated("r2", "CONTROL", Set.of("VIEW"));
        response = new HumanInteractionService().authorize(forbidden, NOW);
        assertEquals(HumanInteractionResponse.ResponseStatus.UNAUTHORIZED, response.status());
    }

    @Test
    void authorizedSoftwareOperationIsOnlyValidatedNotExecuted() {
        HumanInteractionRequest request = requestAuthenticated("r3", "CHANGE_VIEW", Set.of("CHANGE_VIEW"));
        HumanInteractionResponse response = new HumanInteractionService().authorize(request, NOW);
        assertEquals(HumanInteractionResponse.ResponseStatus.ANSWERED, response.status());
        assertTrue(response.groundingSummary().contains("authorization"));
    }

    @Test
    void contextDoesNotGrantPermission() {
        HumanInteractionRequest request = new HumanInteractionRequest(
                "r4", "session-1", AuthenticationState.AUTHENTICATED,
                new AuthorizationContext(Set.of("VIEW")), InputModality.TEXT,
                "Do the restricted operation", "CONTROL", "area-a", NOW, "context:test");
        assertFalse(request.authorized());
    }

    @Test
    void sessionLifecycleIsExplicitAndImmutable() {
        InteractionSession created = InteractionSession.create("session-1", NOW);
        assertEquals(InteractionSessionState.CREATED, created.state());
        assertFalse(created.acceptsInteraction());

        InteractionSession active = created.activate(NOW.plusSeconds(1));
        assertEquals(InteractionSessionState.ACTIVE, active.state());
        assertTrue(active.acceptsInteraction());
        assertEquals(InteractionSessionState.CREATED, created.state());

        InteractionSession closed = active.close(NOW.plusSeconds(2));
        assertEquals(InteractionSessionState.CLOSED, closed.state());
        assertFalse(closed.acceptsInteraction());
        assertThrows(IllegalStateException.class, () -> closed.touch(NOW.plusSeconds(3)));
    }

    @Test
    void closedSessionCannotProcessRequest() {
        InteractionSession session = InteractionSession.create("session-1", NOW).activate(NOW.plusSeconds(1))
                .close(NOW.plusSeconds(2));
        HumanInteractionResponse response = new HumanInteractionService().evaluate(
                requestAuthenticated("r5", "VIEW", Set.of("VIEW")), session, NOW.plusSeconds(3));
        assertEquals(HumanInteractionResponse.ResponseStatus.UNAVAILABLE, response.status());
    }

    @Test
    void missingScopeProducesExplicitClarification() {
        HumanInteractionRequest request = new HumanInteractionRequest(
                "r6", "session-1", AuthenticationState.AUTHENTICATED,
                new AuthorizationContext(Set.of("ANALYZE")), InputModality.TEXT,
                "Analyze it", "ANALYZE", null, NOW, "text-input");
        InteractionSession session = InteractionSession.create("session-1", NOW).activate(NOW.plusSeconds(1));

        HumanInteractionService service = new HumanInteractionService();
        HumanInteractionResponse response = service.evaluate(request, session, NOW.plusSeconds(2));
        HumanInteractionClarification clarification = service.clarify(request);

        assertEquals(HumanInteractionResponse.ResponseStatus.CLARIFICATION_REQUIRED, response.status());
        assertNotNull(clarification);
        assertEquals(ClarificationReason.MISSING_SCOPE, clarification.reason());
    }

    @Test
    void ambiguousRequestIsNotGuessed() {
        HumanInteractionRequest request = requestAuthenticated("r7", null, Set.of());
        HumanInteractionService service = new HumanInteractionService();
        HumanInteractionClarification clarification = service.clarify(
                new HumanInteractionRequest("r7", "session-1", AuthenticationState.AUTHENTICATED,
                        new AuthorizationContext(Set.of()), InputModality.TEXT,
                        "do it", null, null, NOW, "text-input"));
        assertEquals(ClarificationReason.AMBIGUOUS_REQUEST, clarification.reason());
        assertEquals(HumanInteractionResponse.ResponseStatus.CLARIFICATION_REQUIRED,
                service.evaluate(request, InteractionSession.create("session-1", NOW).activate(NOW.plusSeconds(1)), NOW.plusSeconds(2)).status());
    }

    private static HumanInteractionRequest request(String id, InputModality modality, String operation, Set<String> permissions) {
        return new HumanInteractionRequest(id, "session-1", AuthenticationState.UNAUTHENTICATED,
                new AuthorizationContext(permissions), modality, "What changed?", operation, "area-a", NOW,
                modality == InputModality.SPEECH ? "speech-recognition-v1" : "text-input");
    }

    private static HumanInteractionRequest requestAuthenticated(String id, String operation, Set<String> permissions) {
        return new HumanInteractionRequest(id, "session-1", AuthenticationState.AUTHENTICATED,
                new AuthorizationContext(permissions), InputModality.TEXT, "Do it", operation, "area-a", NOW, "text-input");
    }
}
