package com.buccees.vigil.input;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CorelessInputRouterTest {
    private static CorelessInputEvent pointer(long sequence) {
        return new CorelessInputEvent(
                CorelessInputEvent.ABI_VERSION,
                InputEventKind.POINTER_MOVE,
                "arduino-1",
                1_000 + sequence,
                sequence,
                CoordinateFrame.HOST,
                12.5,
                7.0,
                null,
                null,
                null,
                Map.of("source", "arduino", "wire_event", "pointer_move"));
    }

    private static InputDeviceContext designatedPointer() {
        return new InputDeviceContext(
                "arduino-1",
                "serial-pointer",
                Set.of("pointer", "absolute"),
                true,
                3);
    }

    private static InterpretedInputEvent interpreted(CorelessInputEvent event) {
        return new InterpretedInputEvent(
                "interpret-" + event.sequence(),
                "pointer_move",
                event.deviceId(),
                event.timestampNs(),
                List.of(event.sequence()),
                Map.of("provenance", "raw-event"),
                0.99);
    }

    @Test
    void validRawEventAndDerivedEventRetainProvenance() {
        CorelessInputInterpreter interpreter = new CorelessInputInterpreter() {
            @Override public boolean available() { return true; }
            @Override public List<InterpretedInputEvent> interpret(
                    CorelessInputEvent event, InputDeviceContext device) {
                assertEquals(3, device.assignmentGeneration());
                return List.of(interpreted(event));
            }
        };
        CorelessInputRouter router = new CorelessInputRouter(interpreter, true);

        List<InterpretedInputEvent> result = router.submit(pointer(7), designatedPointer());

        assertEquals(1, router.rawEvents().size());
        assertEquals("arduino-1", router.rawEvents().get(0).deviceId());
        assertEquals(7, router.rawEvents().get(0).sequence());
        assertEquals(1, result.size());
        assertEquals(List.of(7L), result.get(0).sourceSequences());
        assertEquals(1, router.interpretedEvents().size());
        assertTrue(router.lastInterpretationFailure().isEmpty());
    }

    @Test
    void disabledOrUnavailableVigilDoesNotBlockRawInput() {
        CorelessInputInterpreter unavailable = new CorelessInputInterpreter() {
            @Override public boolean available() { return false; }
            @Override public List<InterpretedInputEvent> interpret(
                    CorelessInputEvent event, InputDeviceContext device) {
                fail("unavailable interpreter must not be called");
                return List.of();
            }
        };
        CorelessInputRouter router = new CorelessInputRouter(unavailable, true);
        assertTrue(router.submit(pointer(1), designatedPointer()).isEmpty());
        assertEquals(1, router.rawEvents().size());

        router.setInterpretationEnabled(false);
        assertTrue(router.submit(pointer(2), designatedPointer()).isEmpty());
        assertEquals(2, router.rawEvents().size());
        assertTrue(router.interpretedEvents().isEmpty());

        CorelessInputRouter noInterpreter = new CorelessInputRouter(null, true);
        assertTrue(noInterpreter.submit(pointer(3), designatedPointer()).isEmpty());
        assertEquals(1, noInterpreter.rawEvents().size());
        assertFalse(noInterpreter.interpretationEnabled());
    }

    @Test
    void interpreterFailureFallsBackWithoutDroppingRawEvent() {
        CorelessInputInterpreter broken = new CorelessInputInterpreter() {
            @Override public boolean available() { return true; }
            @Override public List<InterpretedInputEvent> interpret(
                    CorelessInputEvent event, InputDeviceContext device) {
                throw new IllegalStateException("test outage");
            }
        };
        CorelessInputRouter router = new CorelessInputRouter(broken, true);

        assertTrue(router.submit(pointer(4), designatedPointer()).isEmpty());
        assertEquals(1, router.rawEvents().size());
        assertTrue(router.interpretedEvents().isEmpty());
        assertTrue(router.lastInterpretationFailure().orElseThrow().contains("test outage"));
        assertTrue(router.submit(pointer(5), designatedPointer()).isEmpty());
        assertEquals(2, router.rawEvents().size());
    }

    @Test
    void invalidProvenanceIsRejectedAndRawEventStillSurvives() {
        CorelessInputInterpreter badProvenance = new CorelessInputInterpreter() {
            @Override public boolean available() { return true; }
            @Override public List<InterpretedInputEvent> interpret(
                    CorelessInputEvent event, InputDeviceContext device) {
                return List.of(new InterpretedInputEvent(
                        "wrong", "gesture", event.deviceId(), event.timestampNs(),
                        List.of(event.sequence() - 1), Map.of(), null));
            }
        };
        CorelessInputRouter router = new CorelessInputRouter(badProvenance, true);

        assertTrue(router.submit(pointer(8), designatedPointer()).isEmpty());
        assertEquals(1, router.rawEvents().size());
        assertTrue(router.interpretedEvents().isEmpty());
        assertTrue(router.lastInterpretationFailure().isPresent());
    }

    @Test
    void mismatchedDeviceAndUndesignatedInputAreRejectedBeforeRawMutation() {
        CorelessInputRouter router = new CorelessInputRouter(null, false);
        InputDeviceContext wrongDevice = new InputDeviceContext(
                "mouse-0", "mouse", Set.of("pointer"), true, 0);
        assertThrows(SecurityException.class, () -> router.submit(pointer(1), wrongDevice));

        InputDeviceContext notDesignated = new InputDeviceContext(
                "arduino-1", "serial-pointer", Set.of("pointer"), false, 2);
        assertThrows(SecurityException.class, () -> router.submit(pointer(1), notDesignated));
        assertTrue(router.rawEvents().isEmpty());
    }

    @Test
    void rejectsBadAbiCoordinatesAndNonMonotonicSequence() {
        assertThrows(IllegalArgumentException.class, () -> new CorelessInputEvent(
                2, InputEventKind.POINTER_MOVE, "arduino-1", 1, 1,
                CoordinateFrame.HOST, 1.0, 1.0, null, null, null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new CorelessInputEvent(
                1, InputEventKind.POINTER_MOVE, "arduino-1", 1, 1,
                CoordinateFrame.HOST, Double.NaN, 1.0, null, null, null, Map.of()));
        CorelessInputRouter router = new CorelessInputRouter(null, false);
        router.submit(pointer(9), designatedPointer());
        assertThrows(IllegalArgumentException.class, () -> router.submit(pointer(9), designatedPointer()));
        assertEquals(1, router.rawEvents().size());
    }

    @Test
    void deviceStateCanBeRecordedWithoutPointingAssignmentOrInterpretation() {
        CorelessInputEvent state = new CorelessInputEvent(
                1, InputEventKind.DEVICE_STATE, "arduino-sensor", 55, 0,
                CoordinateFrame.HOST, null, null, null, null, null, Map.of("value", 512));
        InputDeviceContext sensor = new InputDeviceContext(
                "arduino-sensor", "sensor", Set.of(), false, 0);
        CorelessInputRouter router = new CorelessInputRouter(
                new CorelessInputInterpreter() {
                    @Override public boolean available() { return true; }
                    @Override public List<InterpretedInputEvent> interpret(
                            CorelessInputEvent event, InputDeviceContext device) {
                        fail("device state must not be interpreted as a gesture");
                        return List.of();
                    }
                }, true);

        assertTrue(router.submit(state, sensor).isEmpty());
        assertEquals(1, router.rawEvents().size());
        assertTrue(router.interpretedEvents().isEmpty());
    }
}
