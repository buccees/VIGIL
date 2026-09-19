package com.buccees.vigil.interaction;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HumanConfirmationBoundaryTest {
    private static final Instant NOW = Instant.parse("2026-09-18T14:00:00Z");

    @Test
    void consequentialOperationStartsAsRequired() {
        HumanInteractionRequest request = request();
        HumanConfirmation confirmation = new HumanConfirmationService()
                .required(request, "change_view", "north-sector", NOW);

        assertEquals(ConfirmationStatus.REQUIRED, confirmation.status());
        assertFalse(confirmation.confirmed());
        assertEquals(request.requestId(), confirmation.requestId());
        assertEquals(request.sessionId(), confirmation.sessionId());
    }

    @Test
    void explicitConfirmationIsRequiredBeforeAnyConsumerCanTreatItAsConfirmed() {
        HumanConfirmationService service = new HumanConfirmationService();
        HumanConfirmationRequest request = new HumanConfirmationRequest(
                "c-1", "r-1", "s-1", "start_analysis", "north-sector", NOW);

        HumanConfirmation required = new HumanConfirmation(
                request.confirmationId(), request.requestId(), request.sessionId(),
                request.operation(), request.scope(), ConfirmationStatus.REQUIRED, NOW);
        assertFalse(required.confirmed());

        HumanConfirmation confirmed = service.confirm(required, request, NOW.plusSeconds(1));
        assertEquals(ConfirmationStatus.CONFIRMED, confirmed.status());
        assertTrue(confirmed.confirmed());
    }

    @Test
    void declineRemainsExplicitlyDeclined() {
        HumanConfirmationRequest request = new HumanConfirmationRequest(
                "c-2", "r-2", "s-2", "change_view", "south-sector", NOW);

        HumanConfirmation required = new HumanConfirmation(
                request.confirmationId(), request.requestId(), request.sessionId(),
                request.operation(), request.scope(), ConfirmationStatus.REQUIRED, NOW);
        HumanConfirmation declined = new HumanConfirmationService().decline(required, request, NOW.plusSeconds(1));

        assertEquals(ConfirmationStatus.DECLINED, declined.status());
        assertFalse(declined.confirmed());
    }

    @Test
    void confirmationDoesNotGrantAuthorizationOrContainAnExecutor() {
        HumanInteractionRequest request = request();
        HumanConfirmation confirmation = new HumanConfirmationService()
                .required(request, "start_analysis", "north-sector", NOW);

        assertFalse(request.authorization().permits("start_analysis"));
        assertFalse(confirmation.getClass().getDeclaredFields().length == 0);
        assertEquals(Set.of("confirmationId", "requestId", "sessionId", "operation", "scope", "status", "decidedAt"),
                Set.of(confirmation.getClass().getRecordComponents()).stream()
                        .map(java.lang.reflect.RecordComponent::getName).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void mismatchedConfirmationCannotConfirmAnotherOperation() {
        HumanConfirmation required = new HumanConfirmation(
                "c-3", "r-3", "s-3", "start_analysis", "north-sector", ConfirmationStatus.REQUIRED, NOW);
        HumanConfirmationRequest mismatched = new HumanConfirmationRequest(
                "c-3", "r-3", "s-3", "change_view", "north-sector", NOW);

        assertThrows(IllegalArgumentException.class,
                () -> new HumanConfirmationService().confirm(required, mismatched, NOW.plusSeconds(1)));
    }

    @Test
    void authorizedOperationStillRequiresExplicitHumanConfirmation() {
        HumanInteractionRequest request = new HumanInteractionRequest(
                "r-10", "s-10", AuthenticationState.AUTHENTICATED,
                new AuthorizationContext(Set.of("start_analysis")), InputModality.TEXT,
                "start analysis", "start_analysis", "north-sector", NOW, "user-text");
        InteractionSession session = InteractionSession.create("s-10", NOW).activate(NOW.plusSeconds(1));

        HumanInteractionResponse response = new HumanInteractionService()
                .evaluate(request, session, NOW.plusSeconds(2));

        assertEquals(HumanInteractionResponse.ResponseStatus.CONFIRMATION_REQUIRED, response.status());
    }

    private HumanInteractionRequest request() {
        return new HumanInteractionRequest(
                "r-1", "s-1", AuthenticationState.AUTHENTICATED,
                new AuthorizationContext(Set.of("view")), InputModality.TEXT,
                "start analysis", "start_analysis", "north-sector", NOW, "user-text");
    }
}
