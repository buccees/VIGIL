package com.buccees.vigil.input;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
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
        Map<String, Object> fields = minimalFields();
        fields.put("sequence", "4");
        assertThrows(IllegalArgumentException.class, () -> CorelessInputEventCodec.decode(fields));

        fields = minimalFields();
        fields.put("metadata", "not-an-object");
        Map<String, Object> stringMetadata = fields;
        assertThrows(IllegalArgumentException.class, () -> CorelessInputEventCodec.decode(stringMetadata));

        fields = minimalFields();
        fields.put("x", Double.POSITIVE_INFINITY);
        fields.put("y", 1.0);
        Map<String, Object> nonFinite = fields;
        assertThrows(IllegalArgumentException.class, () -> CorelessInputEventCodec.decode(nonFinite));
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
