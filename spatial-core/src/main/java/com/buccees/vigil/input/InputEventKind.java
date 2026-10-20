package com.buccees.vigil.input;

/** Wire-compatible event kinds from Coreless Input ABI version 1. */
public enum InputEventKind {
    POINTER_MOVE("pointer_move"),
    POINTER_BUTTON("pointer_button"),
    TOUCH_BEGIN("touch_begin"),
    TOUCH_UPDATE("touch_update"),
    TOUCH_END("touch_end"),
    STYLUS("stylus"),
    DEVICE_STATE("device_state");

    private final String wireValue;

    InputEventKind(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
