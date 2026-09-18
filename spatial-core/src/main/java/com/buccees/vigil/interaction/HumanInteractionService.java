package com.buccees.vigil.interaction;

import java.time.Instant;
import java.util.Objects;

public final class HumanInteractionService {
    public HumanInteractionResponse authorize(HumanInteractionRequest request, Instant now) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(now, "now");

        if (!request.authenticated()) {
            return new HumanInteractionResponse(request.requestId(),
                    HumanInteractionResponse.ResponseStatus.AUTHENTICATION_REQUIRED,
                    "Authentication is required for this request.",
                    now, "Authorization was not evaluated because authentication is absent.");
        }
        if (!request.authorized()) {
            return new HumanInteractionResponse(request.requestId(),
                    HumanInteractionResponse.ResponseStatus.UNAUTHORIZED,
                    "The requested operation is not authorized for this session.",
                    now, "The operation was checked against the session authorization context.");
        }
        return new HumanInteractionResponse(request.requestId(),
                HumanInteractionResponse.ResponseStatus.ANSWERED,
                "Request is authenticated and authorized for processing.",
                now, "Request identity, modality, authentication, authorization, and interpretation provenance are preserved.");
    }

    public HumanInteractionResponse evaluate(HumanInteractionRequest request, InteractionSession session, Instant now) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(now, "now");

        if (!session.sessionId().equals(request.sessionId())) {
            return new HumanInteractionResponse(request.requestId(),
                    HumanInteractionResponse.ResponseStatus.UNAVAILABLE,
                    "The request does not belong to the supplied interaction session.",
                    now, "Session identity mismatch prevented processing.");
        }
        if (!session.acceptsInteraction()) {
            return new HumanInteractionResponse(request.requestId(),
                    HumanInteractionResponse.ResponseStatus.UNAVAILABLE,
                    "The interaction session is not active.",
                    now, "Closed or expired sessions do not accept new interaction.");
        }

        HumanInteractionResponse authorization = authorize(request, now);
        if (authorization.status() != HumanInteractionResponse.ResponseStatus.ANSWERED) {
            return authorization;
        }

        if (request.requestedOperation() != null && request.requestedScope() == null) {
            return new HumanInteractionResponse(request.requestId(),
                    HumanInteractionResponse.ResponseStatus.CLARIFICATION_REQUIRED,
                    "Please specify the scope for the requested operation.",
                    now, "The operation is authorized, but its required scope is unspecified.");
        }

        return authorization;
    }

    public HumanInteractionClarification clarify(HumanInteractionRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.recognizedText().trim().equalsIgnoreCase("do it")
                || request.recognizedText().trim().equalsIgnoreCase("go ahead")) {
            return new HumanInteractionClarification(request.requestId(),
                    ClarificationReason.AMBIGUOUS_REQUEST,
                    "What would you like VIGIL to do?");
        }
        if (request.requestedOperation() != null && request.requestedScope() == null) {
            return new HumanInteractionClarification(request.requestId(),
                    ClarificationReason.MISSING_SCOPE,
                    "What scope, area, entity, or time range should this operation use?");
        }
        return null;
    }
}
