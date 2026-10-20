package com.buccees.vigil.input;

import java.util.List;

/** Optional VIGIL interpreter. Implementations must not claim device authority. */
public interface CorelessInputInterpreter {
    /** Return false while disabled, disconnected, or otherwise unavailable. */
    boolean available();

    /** Interpret a validated raw event with the current Coreless device context. */
    List<InterpretedInputEvent> interpret(
            CorelessInputEvent event,
            InputDeviceContext device
    );
}
