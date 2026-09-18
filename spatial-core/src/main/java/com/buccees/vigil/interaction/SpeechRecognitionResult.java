package com.buccees.vigil.interaction;

import java.time.Instant;

public record SpeechRecognitionResult(
        String transcript,
        double confidence,
        String provenance,
        Instant recognitionTime
) {
    public SpeechRecognitionResult {
        if (transcript == null || transcript.isBlank()) throw new IllegalArgumentException("transcript must not be blank");
        if (!Double.isFinite(confidence) || confidence < 0.0 || confidence > 1.0)
            throw new IllegalArgumentException("confidence must be between 0 and 1");
        if (provenance == null || provenance.isBlank()) throw new IllegalArgumentException("provenance must not be blank");
        if (recognitionTime == null) throw new IllegalArgumentException("recognitionTime must not be null");
    }
}
