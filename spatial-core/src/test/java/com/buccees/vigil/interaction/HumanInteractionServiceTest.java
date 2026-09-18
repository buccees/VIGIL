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
