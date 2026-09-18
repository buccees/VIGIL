package com.buccees.vigil.interaction;

import java.time.Instant;
import java.util.Objects;

public record SpeechSynthesisRequest(
        String responseId,
        String sessionId,
        String text,
        String groundingSummary,
        Instant synthesisTime
) {
    public SpeechSynthesisRequest {
        if (responseId == null || responseId.isBlank()) throw new IllegalArgumentException("responseId must not be blank");
        if (sessionId == null || sessionId.isBlank()) throw new IllegalArgumentException("sessionId must not be blank");
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text must not be blank");
        if (groundingSummary == null || groundingSummary.isBlank()) throw new IllegalArgumentException("groundingSummary must not be blank");
        Objects.requireNonNull(synthesisTime, "synthesisTime");
    }

    public static SpeechSynthesisRequest from(HumanInteractionResponse response, String sessionId, Instant now) {
        Objects.requireNonNull(response, "response");
        return new SpeechSynthesisRequest(
                response.requestId(),
                sessionId,
                response.message(),
                response.groundingSummary(),
                now);
    }
}
