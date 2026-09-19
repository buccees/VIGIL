package com.buccees.vigil.interaction;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class VoiceAdapterBoundaryTest {
    @Test
    void speechRecognitionFailureIsContainedAtVoiceBoundary() {
        VoiceInputAdapter adapter = request -> {
            throw new IllegalStateException("speech provider unavailable");
        };

        VoiceInputRequest request = new VoiceInputRequest(
                "voice-4", "session-1", MicrophonePermissionState.GRANTED, NOW);
        VoiceInputResult result = new VoiceInputGateway(adapter).recognize(request);

        assertEquals(VoiceInputStatus.RECOGNITION_FAILED, result.status());
        assertTrue(result.recognizedSpeech().isEmpty());
    }

    private static final Instant NOW = Instant.parse("2026-09-18T12:00:00Z");

    @Test
    void grantedMicrophonePermissionReachesAdapterAndPreservesRecognitionProvenance() {
        AtomicBoolean invoked = new AtomicBoolean();
        VoiceInputAdapter adapter = request -> {
            invoked.set(true);
            return new SpeechRecognitionResult("show me the north sector", 0.42,
                    "local-speech-recognizer:v1", NOW.plusSeconds(1));
        };

        VoiceInputRequest request = new VoiceInputRequest(
                "voice-1", "session-1", MicrophonePermissionState.GRANTED, NOW);
        VoiceInputResult result = new VoiceInputGateway(adapter).recognize(request);

        assertTrue(invoked.get());
        assertEquals(VoiceInputStatus.RECOGNIZED, result.status());
        assertEquals("show me the north sector", result.recognizedSpeech().orElseThrow().transcript());
        assertEquals(0.42, result.recognizedSpeech().orElseThrow().confidence());
        assertEquals("local-speech-recognizer:v1", result.recognizedSpeech().orElseThrow().provenance());
    }

    @Test
    void deniedMicrophonePermissionDoesNotInvokeAdapterOrProduceSpeechData() {
        AtomicBoolean invoked = new AtomicBoolean();
        VoiceInputAdapter adapter = request -> {
            invoked.set(true);
            throw new AssertionError("adapter must not receive denied microphone input");
        };

        VoiceInputRequest request = new VoiceInputRequest(
                "voice-2", "session-1", MicrophonePermissionState.DENIED, NOW);
        VoiceInputResult result = new VoiceInputGateway(adapter).recognize(request);

        assertFalse(invoked.get());
        assertEquals(VoiceInputStatus.MICROPHONE_NOT_PERMITTED, result.status());
        assertTrue(result.recognizedSpeech().isEmpty());
    }

    @Test
    void speechOutputPreservesGroundingWhenConvertedFromResponse() {
        HumanInteractionResponse response = new HumanInteractionResponse(
                "req-7",
                HumanInteractionResponse.ResponseStatus.ANSWERED,
                "Three detections were recorded.",
                NOW,
                "Grounded in current world-state observations.");

        SpeechSynthesisRequest synthesis =
                SpeechSynthesisRequest.from(response, "session-7", NOW.plusSeconds(1));

        assertEquals("req-7", synthesis.responseId());
        assertEquals("session-7", synthesis.sessionId());
        assertEquals(response.message(), synthesis.text());
        assertEquals(response.groundingSummary(), synthesis.groundingSummary());
    }

    @Test
    void lowConfidenceRecognitionRemainsExplicitAndDoesNotGrantAnOperation() {
        SpeechRecognitionResult recognition = new SpeechRecognitionResult(
                "do it", 0.15, "test-speech-recognizer", NOW);

        HumanInteractionRequest request = new HumanInteractionRequest(
                "voice-3",
                "session-1",
                AuthenticationState.AUTHENTICATED,
                new AuthorizationContext(java.util.Set.of()),
                InputModality.SPEECH,
                recognition.transcript(),
                null,
                null,
                NOW,
                "speech:" + recognition.provenance());

        assertEquals(InputModality.SPEECH, request.modality());
        assertEquals(0.15, recognition.confidence());
        assertNull(request.requestedOperation());
        assertTrue(request.authorized());
    }
}
