package com.buccees.vigil.input;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JSON-library-neutral field codec for the Coreless Input ABI v1.
 *
 * The host transport parses JSON into a map; this codec validates that object
 * and maps its snake_case ABI fields to the VIGIL Java event model.
 */
public final class CorelessInputEventCodec {
    private CorelessInputEventCodec() { }

    public static CorelessInputEvent decode(Map<String, ?> fields) {
        Objects.requireNonNull(fields, "fields");
        int version = requiredInt(fields, "abi_version");
        InputEventKind type = enumByWireValue(
                InputEventKind.values(), requiredString(fields, "event_type"),
                InputEventKind::wireValue, "event_type");
        String deviceId = requiredString(fields, "device_id");
        long timestamp = requiredLong(fields, "timestamp_ns");
        long sequence = requiredLong(fields, "sequence");
        CoordinateFrame frame = enumByWireValue(
                CoordinateFrame.values(), requiredString(fields, "coordinate_frame"),
                CoordinateFrame::wireValue, "coordinate_frame");
        Double x = optionalNumber(fields, "x");
        Double y = optionalNumber(fields, "y");
        Integer contactId = optionalInt(fields, "contact_id");
        Double pressure = optionalNumber(fields, "pressure");
        Integer button = optionalInt(fields, "button");
        Object rawMetadata = fields.get("metadata");
        Map<String, Object> metadata;
        if (rawMetadata == null) {
            metadata = Map.of();
        } else if (rawMetadata instanceof Map<?, ?> source) {
            metadata = new LinkedHashMap<>();
            for (Map.Entry<?, ?> item : source.entrySet()) {
                if (!(item.getKey() instanceof String key) || key.isBlank()) {
                    throw new IllegalArgumentException("metadata keys must be non-blank strings");
                }
                metadata.put(key, item.getValue());
            }
        } else {
            throw new IllegalArgumentException("metadata must be an object");
        }
        return new CorelessInputEvent(version, type, deviceId, timestamp, sequence,
                frame, x, y, contactId, pressure, button, metadata);
    }

    public static Map<String, Object> encode(CorelessInputEvent event) {
        Objects.requireNonNull(event, "event");
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("abi_version", event.abiVersion());
        fields.put("event_type", event.eventType().wireValue());
        fields.put("device_id", event.deviceId());
        fields.put("timestamp_ns", event.timestampNs());
        fields.put("sequence", event.sequence());
        fields.put("coordinate_frame", event.coordinateFrame().wireValue());
        fields.put("x", event.x());
        fields.put("y", event.y());
        fields.put("contact_id", event.contactId());
        fields.put("pressure", event.pressure());
        fields.put("button", event.button());
        fields.put("metadata", event.metadata());
        return Collections.unmodifiableMap(fields);
    }

    private static String requiredString(Map<String, ?> fields, String key) {
        Object value = fields.get(key);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(key + " must be a non-blank string");
        }
        return text;
    }

    private static long requiredLong(Map<String, ?> fields, String key) {
        Object value = fields.get(key);
        if (!(value instanceof Number number) || value instanceof Double || value instanceof Float) {
            throw new IllegalArgumentException(key + " must be an integer");
        }
        try {
            if (number instanceof java.math.BigInteger integer) return integer.longValueExact();
            if (number instanceof java.math.BigDecimal decimal) return decimal.longValueExact();
            return number.longValue();
        } catch (ArithmeticException failure) {
            throw new IllegalArgumentException(key + " is outside the supported integer range", failure);
        }
    }

    private static int requiredInt(Map<String, ?> fields, String key) {
        long value = requiredLong(fields, key);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(key + " is outside the integer range");
        }
        return (int) value;
    }

    private static Integer optionalInt(Map<String, ?> fields, String key) {
        if (!fields.containsKey(key) || fields.get(key) == null) return null;
        long value = requiredLong(fields, key);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(key + " is outside the integer range");
        }
        return (int) value;
    }

    private static Double optionalNumber(Map<String, ?> fields, String key) {
        if (!fields.containsKey(key) || fields.get(key) == null) return null;
        Object value = fields.get(key);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException(key + " must be a number or null");
        }
        double converted = number.doubleValue();
        if (!Double.isFinite(converted)) {
            throw new IllegalArgumentException(key + " must be finite");
        }
        return converted;
    }

    private static <E> E enumByWireValue(
            E[] values, String wireValue, java.util.function.Function<E, String> lookup, String field) {
        for (E value : values) {
            if (lookup.apply(value).equals(wireValue)) return value;
        }
        throw new IllegalArgumentException("unsupported " + field + ": " + wireValue);
    }
}
