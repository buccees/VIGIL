package com.buccees.vigil.input;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CorelessInputEventCodecTest {
    @Test
    void roundTripsCorelessSnakeCaseFields() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("abi_version", 1);
        input.put("event_type", "touch_update");
        input.put("device_id", "touch-1");
        input.put("timestamp_ns", 900L);
        input.put("sequence", 12L);
        input.put("coordinate_frame", "display");
        input.put("x", 12.5);
        input.put("y", 44.0);
        input.put("contact_id", 3);
        input.put("pressure", 0.4);
        input.put("button", null);
        input.put("metadata", Map.of("source", "coreless", "raw", true));

        CorelessInputEvent event = CorelessInputEventCodec.decode(input);
        Map<String, Object> encoded = CorelessInputEventCodec.encode(event);
        CorelessInputEvent restored = CorelessInputEventCodec.decode(encoded);

        assertEquals(InputEventKind.TOUCH_UPDATE, event.eventType());
        assertEquals("touch-1", event.deviceId());
        assertEquals(12L, event.sequence());
        assertEquals(12.5, event.x());
        assertEquals(3, event.contactId());
        assertEquals(event, restored);
        assertEquals("touch_update", encoded.get("event_type"));
        assertEquals("display", encoded.get("coordinate_frame"));
    }

    @Test
    void rejectsUnknownEventAndCoordinateFrame() {
        Map<String, Object> fields = minimalFields();
        fields.put("event_type", "execute_command");
        assertThrows(IllegalArgumentException.class, () -> CorelessInputEventCodec.decode(fields));

        fields.put("event_type", "pointer_move");
        fields.put("coordinate_frame", "host-os-authority");
        assertThrows(IllegalArgumentException.class, () -> CorelessInputEventCodec.decode(fields));
    }

    @Test
    void rejectsStringIntegersMalformedMetadataAndNonFiniteCoordinates() {
        Map<String, Object> stringInteger = minimalFields();
        stringInteger.put("sequence", "4");
        assertThrows(IllegalArgumentException.class, () -> CorelessInputEventCodec.decode(stringInteger));

        Map<String, Object> stringMetadata = minimalFields();
        stringMetadata.put("metadata", "not-an-object");
        assertThrows(IllegalArgumentException.class, () -> CorelessInputEventCodec.decode(stringMetadata));

        Map<String, Object> nonFinite = minimalFields();
        nonFinite.put("x", Double.POSITIVE_INFINITY);
        nonFinite.put("y", 1.0);
        assertThrows(IllegalArgumentException.class, () -> CorelessInputEventCodec.decode(nonFinite));
    }
    @Test
    void rawEventMetadataIsCopiedAndReadOnly() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "coreless");
        CorelessInputEvent event = new CorelessInputEvent(
                1, InputEventKind.POINTER_MOVE, "mouse-0", 1, 1,
                CoordinateFrame.HOST, 1.0, 2.0, null, null, null, metadata);

        metadata.put("source", "mutated");
        metadata.put("added", true);

        assertEquals("coreless", event.metadata().get("source"));
        assertFalse(event.metadata().containsKey("added"));
        assertThrows(UnsupportedOperationException.class,
                () -> event.metadata().put("source", "tampered"));
    }

    @Test
    void nestedMetadataIsFrozenButEncoderReturnsJsonNativeContainers() {
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("reading", 512);
        java.util.List<Object> history = new java.util.ArrayList<>(java.util.List.of(1, 2));
        nested.put("history", history);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("payload", nested);

        CorelessInputEvent event = new CorelessInputEvent(
                1, InputEventKind.DEVICE_STATE, "arduino-1", 10, 1,
                CoordinateFrame.HOST, null, null, null, null, null, metadata);
        nested.put("reading", 0);
        history.add(3);

        @SuppressWarnings("unchecked")
        Map<String, Object> snapshotPayload = (Map<String, Object>) event.metadata().get("payload");
        assertEquals(512, snapshotPayload.get("reading"));
        assertEquals(List.of(1, 2), snapshotPayload.get("history"));
        assertThrows(UnsupportedOperationException.class,
                () -> snapshotPayload.put("reading", 0));
        @SuppressWarnings("unchecked")
        List<Object> snapshotHistory = (List<Object>) snapshotPayload.get("history");
        assertThrows(UnsupportedOperationException.class, () -> snapshotHistory.add(3));

        @SuppressWarnings("unchecked")
        Map<String, Object> wireMetadata = (Map<String, Object>) CorelessInputEventCodec.encode(event).get("metadata");
        @SuppressWarnings("unchecked")
        Map<String, Object> wirePayload = (Map<String, Object>) wireMetadata.get("payload");
        assertEquals(List.of(1, 2), wirePayload.get("history"));
        assertDoesNotThrow(() -> wirePayload.put("serializer-ready", true));
    }

    private static Map<String, Object> minimalFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("abi_version", 1);
        fields.put("event_type", "pointer_move");
        fields.put("device_id", "mouse-0");
        fields.put("timestamp_ns", 1L);
        fields.put("sequence", 1L);
        fields.put("coordinate_frame", "host");
        fields.put("x", 1.0);
        fields.put("y", 2.0);
        fields.put("metadata", Map.of());
        return fields;
    }
}
