package com.buccees.vigil.interaction;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public record HumanInteractionRequest(
        String requestId,
        String sessionId,
        AuthenticationState authentication,
        AuthorizationContext authorization,
        InputModality modality,
        String recognizedText,
        String requestedOperation,
        String requestedScope,
        Instant requestTime,
        String interpretationProvenance
) {
    public HumanInteractionRequest {
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId must not be blank");
        if (sessionId == null || sessionId.isBlank()) throw new IllegalArgumentException("sessionId must not be blank");
        Objects.requireNonNull(authentication, "authentication");
        Objects.requireNonNull(authorization, "authorization");
        Objects.requireNonNull(modality, "modality");
        if (recognizedText == null || recognizedText.isBlank()) throw new IllegalArgumentException("recognizedText must not be blank");
        Objects.requireNonNull(requestTime, "requestTime");
        if (interpretationProvenance == null || interpretationProvenance.isBlank())
            throw new IllegalArgumentException("interpretationProvenance must not be blank");
    }

    public boolean authenticated() {
        return authentication == AuthenticationState.AUTHENTICATED;
    }

    public boolean authorized() {
        return requestedOperation == null || authorization.permits(requestedOperation);
    }

    public Optional<String> operation() {
        return Optional.ofNullable(requestedOperation);
    }
}
