package com.buccees.vigil.interaction;

import java.util.Objects;

public final class VoiceInputGateway {
    private final VoiceInputAdapter adapter;

    public VoiceInputGateway(VoiceInputAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public VoiceInputResult recognize(VoiceInputRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.microphonePermission() == MicrophonePermissionState.DENIED) {
            return new VoiceInputResult(VoiceInputStatus.MICROPHONE_NOT_PERMITTED, null);
        }
        try {
            return new VoiceInputResult(VoiceInputStatus.RECOGNIZED, adapter.recognize(request));
        } catch (RuntimeException failure) {
            return new VoiceInputResult(VoiceInputStatus.RECOGNITION_FAILED, null);
        }
    }
}
