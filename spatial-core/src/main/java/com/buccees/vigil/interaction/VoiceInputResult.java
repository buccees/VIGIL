package com.buccees.vigil.interaction;

import java.util.Objects;
import java.util.Optional;

public record VoiceInputResult(
        VoiceInputStatus status,
        SpeechRecognitionResult recognition
) {
    public VoiceInputResult {
        Objects.requireNonNull(status, "status");
        if (status == VoiceInputStatus.RECOGNIZED && recognition == null)
            throw new IllegalArgumentException("recognized input requires recognition result");
        if (status == VoiceInputStatus.MICROPHONE_NOT_PERMITTED && recognition != null)
            throw new IllegalArgumentException("denied microphone input must not contain recognition result");
    }

    public Optional<SpeechRecognitionResult> recognizedSpeech() {
        return Optional.ofNullable(recognition);
    }
}
