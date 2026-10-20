package com.buccees.vigil.input;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Current, non-persistent device context negotiated by Coreless.
 *
 * The designated-device assignment remains owned by Coreless; this context
 * only tells VIGIL which device and capabilities are valid for interpretation.
 */
public record InputDeviceContext(
        String deviceId,
        String deviceType,
        Set<String> capabilities,
        boolean designatedPointingDevice,
        long assignmentGeneration
) {
    public InputDeviceContext {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId must not be blank");
        }
        if (deviceType == null || deviceType.isBlank()) {
            throw new IllegalArgumentException("deviceType must not be blank");
        }
        Objects.requireNonNull(capabilities, "capabilities");
        if (capabilities.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("capabilities must not contain blank values");
        }
        capabilities = capabilities.stream().collect(Collectors.toUnmodifiableSet());
        if (assignmentGeneration < 0) {
            throw new IllegalArgumentException("assignmentGeneration must be non-negative");
        }
    }

    public boolean supports(InputEventKind kind) {
        return switch (kind) {
            case POINTER_MOVE -> capabilities.contains("pointer");
            case POINTER_BUTTON -> capabilities.contains("pointer")
                    && capabilities.contains("buttons");
            case TOUCH_BEGIN, TOUCH_UPDATE, TOUCH_END ->
                    capabilities.contains("touch");
            case STYLUS -> capabilities.contains("stylus");
            case DEVICE_STATE -> true;
        };
    }
}
