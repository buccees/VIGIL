# VIGIL — Development Handoff

## Current project state

VIGIL is a spatial intelligence platform and system-level spatial intelligence layer. The current Java/Gradle `spatial-core` implementation is building the deterministic spatial foundation.

The next architectural work must also account for VIGIL operating as an intelligence layer for the Coreless-64 computer.

Coreless integration is **not a separate project**. VIGIL remains its own repository and implementation, while Coreless owns the computer/input boundary. Both sides must implement compatible contracts.

## Coreless-64 input integration

Coreless-64 is being designed as a self-contained digital computer whose host supplies external I/O such as display, keyboard/input, and network connectivity. Coreless owns the computational and semantic boundary; the host does not become Coreless CPU, RAM, OS, VM/hypervisor, or AI runtime.

Coreless is adding a **user-designated pointing-device subsystem**. A user can designate a physical host device as the primary pointing/touch device, and that assignment can persist as part of Coreless machine state.

VIGIL is intended to operate as an **optional input interpretation layer** above that Coreless input boundary.

Target flow:

    HOST PHYSICAL INPUT
            ↓
    CORELESS HOST INPUT TRANSPORT
            ↓
    CORELESS INPUT EVENT ABI
            ↓
    USER-DESIGNATED POINTING DEVICE
            ↓
    VIGIL INPUT INTERPRETATION
            ↓
    CORELESS GUI / APPLICATION INPUT

The important ownership rule is:

> Coreless owns the input architecture and device boundary. VIGIL interprets input when enabled.

VIGIL must not become the physical device, host OS input authority, or Coreless execution authority.

## VIGIL capabilities required for Coreless

The following capabilities need to be designed into VIGIL.

### 1. Coreless input adapter

VIGIL needs a defined integration boundary capable of:

- receiving Coreless input events;
- receiving pointer, touch, stylus, and future compatible input events;
- validating event versions and capabilities;
- returning interpreted events/gestures/intents to Coreless;
- handling connection, disconnection, and degraded states.

### 2. Pointing-device awareness

VIGIL needs to understand the Coreless-designated pointing device without owning the device itself.

The interface should support:

- device identity;
- device capabilities;
- device type;
- absolute/relative coordinate capability;
- touch/stylus/multi-touch capability;
- assignment state;
- attach/detach;
- reassignment;
- failover;
- capability changes.

The persistent identity/assignment remains a Coreless concern; VIGIL consumes the negotiated device context.

### 3. Raw input event model

VIGIL must be able to receive raw, deterministic input before interpretation.

The architecture should define events for at least:

- pointer movement;
- pointer button press/release;
- touch contact begin/update/end;
- touch position;
- pressure where available;
- stylus/pen state where available;
- contact identity;
- timestamps;
- coordinate frame;
- device identity;
- event sequence/order information.

Raw events must remain available so VIGIL interpretation does not become a mandatory dependency for ordinary Coreless input.

### 4. Touch and gesture interpretation

VIGIL should provide an interpretation layer capable of recognizing:

- tap;
- double tap;
- long press;
- drag;
- swipe;
- multi-touch;
- pinch/zoom;
- rotation;
- gesture combinations;
- future extensible gestures.

Gesture recognition must preserve enough source information to explain or reproduce the interpretation.

### 5. Spatial pointing

VIGIL should support:

- absolute pointer coordinates;
- relative pointer movement;
- touch-to-pointer mapping;
- coordinate normalization;
- coordinate-frame metadata;
- spatial relationships between contacts;
- display/input coordinate transformations where negotiated.

This should fit naturally with VIGIL's existing spatial-coordinate architecture.

### 6. VIGIL → Coreless interpreted-event contract

The output side needs a versioned contract for events such as:

- raw input;
- normalized input;
- recognized gesture;
- interpreted intent;
- input context;
- confidence/quality where meaningful;
- source device;
- timestamps;
- event sequence;
- provenance.

The contract must distinguish **what physically happened** from **what VIGIL interpreted it to mean**.

A gesture or intent must never overwrite the raw event history.

### 7. Optional operation and fallback

VIGIL must remain optional.

Coreless must continue to support ordinary pointer/touch operation when VIGIL is unavailable, disconnected, disabled, or unable to interpret an event.

The integration should therefore support:

- VIGIL available;
- VIGIL unavailable;
- VIGIL temporarily degraded;
- interpretation disabled;
- raw-input-only operation;
- reconnect/resume.

### 8. Persistence and resume

Coreless owns persistent machine state, but VIGIL must define which input interpretation state can safely be restored.

Potential persisted state includes:

- input preferences;
- gesture configuration;
- interpretation configuration;
- device-independent user preferences;
- negotiated capability state where appropriate.

Device identity itself should not be silently treated as portable hardware identity across unrelated hosts. The architecture must distinguish persistent user designation from the current host's discovered physical device.

### 9. Determinism and testing

The Coreless integration needs deterministic tests for:

- raw event ordering;
- device assignment;
- attach/detach;
- reassignment;
- gesture recognition;
- multi-touch sequences;
- malformed/unsupported events;
- VIGIL unavailable fallback;
- reconnect/resume;
- coordinate transformations;
- interpretation reproducibility.

A recorded Coreless input sequence should be replayable through VIGIL for regression testing.

## Architecture work to do next

Before implementing the adapter, define the contracts.

1. **Coreless Input Event ABI**
   - event types;
   - field definitions;
   - timestamps;
   - coordinate frames;
   - device identity;
   - sequence ordering;
   - capability/version negotiation.

2. **Device Assignment Contract**
   - how Coreless designates a primary pointing device;
   - how VIGIL receives the designation;
   - attach/detach;
   - reassignment;
   - failover.

3. **VIGIL Input Interpretation API**
   - input ingestion;
   - normalized events;
   - gesture recognition;
   - interpreted intents;
   - context;
   - confidence/quality;
   - provenance.

4. **Raw vs. interpreted boundary**
   - raw events remain authoritative input evidence;
   - VIGIL interpretations remain derived information;
   - applications can choose raw or interpreted input.

5. **Spatial mapping**
   - device coordinates;
   - Coreless coordinates;
   - display coordinates;
   - VIGIL spatial coordinates;
   - transformations and calibration.

6. **Lifecycle**
   - discover;
   - negotiate;
   - attach;
   - operate;
   - detach;
   - resume;
   - reattach.

7. **Security/authority boundary**
   - Coreless remains authoritative for device assignment and input routing;
   - VIGIL interprets information;
   - VIGIL must not silently gain host-device authority.

## Architectural principle

The intended relationship is:

    CORELESS
    Computer + input boundary + device assignment
                    │
                    │ input events
                    ▼
    VIGIL
    Interpretation + spatial context + gesture intelligence
                    │
                    │ interpreted input
                    ▼
    CORELESS
    GUI + applications

This should be treated as a first-class integration contract, not a one-off adapter.

## Immediate resume point

**Do not jump straight into implementation.**

First work through the architecture and settle the Coreless/VIGIL contracts, especially:

- event ABI;
- device designation;
- raw versus interpreted input;
- coordinate systems;
- gesture representation;
- lifecycle;
- persistence;
- fallback behavior.

Once those contracts are stable, implement the VIGIL-side capabilities and corresponding Coreless-side adapter/tests together.

## Relationship to existing VIGIL architecture

This integration should extend VIGIL's existing principles rather than bypass them.

The input subsystem is an information/interpretation interface. It should preserve:

- provenance;
- timestamps;
- deterministic behavior where practical;
- uncertainty/quality where applicable;
- separation between observed input and derived interpretation;
- human decision authority.

VIGIL's existing spatial intelligence pipeline remains authoritative for environmental information. Coreless input interpretation is an additional human-machine interaction capability that can use VIGIL's spatial reasoning without turning VIGIL into an autonomous action system.

## Important project boundary

ComfyUI is not part of this Coreless/VIGIL integration.

Coreless AI/model work, Qwen3 work, persistent-storage work, and VIGIL integration remain part of the broader Coreless development effort where applicable. ComfyUI remains separate.
