package com.buccees.vigil.input;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Deep-copy and freeze JSON-compatible metadata carried by input records. */
final class MetadataSnapshot {
    private MetadataSnapshot() { }

    static Map<String, Object> freezeMap(Map<String, ?> source) {
        if (source == null) return Map.of();
        return castMap(freezeValue(source));
    }

    private static Object freezeValue(Object value) {
        if (value == null || value instanceof String || value instanceof Boolean
                || value instanceof Byte || value instanceof Short
                || value instanceof Integer || value instanceof Long
                || value instanceof BigInteger || value instanceof BigDecimal) {
            return value;
        }
        if (value instanceof Double number) {
            if (!Double.isFinite(number)) throw new IllegalArgumentException("metadata numbers must be finite");
            return number;
        }
        if (value instanceof Float number) {
            if (!Float.isFinite(number)) throw new IllegalArgumentException("metadata numbers must be finite");
            return number;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key) || key.isBlank()) {
                    throw new IllegalArgumentException("metadata keys must be non-blank strings");
                }
                copy.put(key, freezeValue(entry.getValue()));
            }
            return Collections.unmodifiableMap(copy);
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            for (Object item : list) copy.add(freezeValue(item));
            return Collections.unmodifiableList(copy);
        }
        throw new IllegalArgumentException("metadata values must be JSON-compatible");
    }

    /** Returns mutable JSON-native maps/lists suitable for a JSON serializer. */
    static Object toJsonValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw new IllegalArgumentException("metadata keys must be strings");
                }
                copy.put(key, toJsonValue(entry.getValue()));
            }
            return copy;
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            for (Object item : list) copy.add(toJsonValue(item));
            return copy;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }
}
