package com.buccees.vigil.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * VIGIL-side integration boundary for Coreless input.
 *
 * Raw events are retained before optional interpretation. If VIGIL is disabled,
 * unavailable, or fails, callers can continue using the raw Coreless path.
 */
public final class CorelessInputRouter {
    public static final int VERSION = 1;

    private final CorelessInputInterpreter interpreter;
    private final List<CorelessInputEvent> rawEvents = new ArrayList<>();
    private final List<InterpretedInputEvent> interpretedEvents = new ArrayList<>();
    private final Map<String, Long> lastSequenceByDevice = new HashMap<>();
    private boolean interpretationEnabled;
    private String lastInterpretationFailure;

    public CorelessInputRouter(CorelessInputInterpreter interpreter, boolean interpretationEnabled) {
        this.interpreter = interpreter;
        this.interpretationEnabled = interpretationEnabled && interpreter != null;
    }

    public synchronized void setInterpretationEnabled(boolean enabled) {
        this.interpretationEnabled = enabled && interpreter != null;
    }

    public synchronized boolean interpretationEnabled() {
        return interpretationEnabled;
    }

    public synchronized List<CorelessInputEvent> rawEvents() {
        return List.copyOf(rawEvents);
    }

    public synchronized List<InterpretedInputEvent> interpretedEvents() {
        return List.copyOf(interpretedEvents);
    }

    public synchronized Optional<String> lastInterpretationFailure() {
        return Optional.ofNullable(lastInterpretationFailure);
    }

    /**
     * Accept one raw event authorized by the current Coreless device context.
     * Validation errors are rejected; interpreter failures do not drop the raw event.
     */
    public synchronized List<InterpretedInputEvent> submit(
            CorelessInputEvent event,
            InputDeviceContext device
    ) {
        if (event == null || device == null) {
            throw new IllegalArgumentException("event and device context are required");
        }
        if (!event.deviceId().equals(device.deviceId())) {
            throw new SecurityException("event device does not match Coreless device context");
        }
        if (!device.supports(event.eventType())) {
            throw new IllegalArgumentException("event type is not supported by device capabilities");
        }
        if (event.eventType() != InputEventKind.DEVICE_STATE && !device.designatedPointingDevice()) {
            throw new SecurityException("interactive input did not come from the designated pointing device");
        }
        Long previous = lastSequenceByDevice.get(event.deviceId());
        if (previous != null && event.sequence() <= previous) {
            throw new IllegalArgumentException("input sequence must increase monotonically per device");
        }

        // The raw event is the authority. Record it before consulting optional VIGIL.
        rawEvents.add(event);
        lastSequenceByDevice.put(event.deviceId(), event.sequence());

        if (event.eventType() == InputEventKind.DEVICE_STATE
                || !interpretationEnabled || interpreter == null) {
            return List.of();
        }

        final boolean available;
        try {
            available = interpreter.available();
        } catch (RuntimeException failure) {
            rememberFailure(failure);
            return List.of();
        }
        if (!available) {
            return List.of();
        }

        try {
            List<InterpretedInputEvent> produced = interpreter.interpret(event, device);
            if (produced == null) {
                throw new IllegalStateException("VIGIL interpreter returned null");
            }
            List<InterpretedInputEvent> checked = new ArrayList<>(produced.size());
            for (InterpretedInputEvent derived : produced) {
                if (derived == null) {
                    throw new IllegalStateException("VIGIL interpreter returned a null event");
                }
                if (!derived.sourceDeviceId().equals(event.deviceId())) {
                    throw new SecurityException("derived event source does not match raw input device");
                }
                if (!derived.sourceSequences().contains(event.sequence())) {
                    throw new IllegalArgumentException("derived event provenance omits triggering raw sequence");
                }
                if (derived.sourceSequences().get(derived.sourceSequences().size() - 1) > event.sequence()) {
                    throw new IllegalArgumentException("derived event provenance references a future sequence");
                }
                checked.add(derived);
            }
            interpretedEvents.addAll(checked);
            lastInterpretationFailure = null;
            return List.copyOf(checked);
        } catch (RuntimeException failure) {
            rememberFailure(failure);
            return List.of();
        }
    }

    private void rememberFailure(RuntimeException failure) {
        String detail = failure.getMessage();
        lastInterpretationFailure = failure.getClass().getSimpleName()
                + (detail == null || detail.isBlank() ? "" : ": " + detail);
    }
}
