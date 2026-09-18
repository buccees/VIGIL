package com.buccees.vigil.interaction;

import java.time.Instant;
import java.util.Objects;

public record VoiceInputRequest(
        String requestId,
        String sessionId,
        MicrophonePermissionState microphonePermission,
        Instant captureTime
) {
    public VoiceInputRequest {
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId must not be blank");
        if (sessionId == null || sessionId.isBlank()) throw new IllegalArgumentException("sessionId must not be blank");
        Objects.requireNonNull(microphonePermission, "microphonePermission");
        Objects.requireNonNull(captureTime, "captureTime");
    }
}
