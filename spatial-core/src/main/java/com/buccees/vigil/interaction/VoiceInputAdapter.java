package com.buccees.vigil.interaction;

public interface VoiceInputAdapter {
    SpeechRecognitionResult recognize(VoiceInputRequest request);
}
