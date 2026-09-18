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
}
