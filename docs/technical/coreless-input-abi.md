# Coreless Input ABI v1 — VIGIL Adapter Contract

## Ownership

Coreless is authoritative for host input transport, physical-device discovery, user designation, event ordering, persistence, and raw application delivery. VIGIL is an optional interpreter of already-validated Coreless events. This adapter cannot designate physical devices, invent missing hardware capabilities, or authorize Coreless actions.

The Java contract is implemented under com.buccees.vigil.input in the feature/coreless-input-contract branch.

## Raw event mapping

| Coreless field | Java field | Contract |
|---|---|---|
| abi_version | abiVersion | Must be exactly 1 |
| event_type | eventType | Wire values: pointer_move, pointer_button, touch_begin, touch_update, touch_end, stylus, device_state |
| device_id | deviceId | Non-blank logical Coreless device identifier |
| timestamp_ns | timestampNs | Non-negative monotonic timestamp in nanoseconds |
| sequence | sequence | Non-negative, strictly increasing for each device |
| coordinate_frame | coordinateFrame | One of host, coreless, display, vigil |
| x, y | x, y | Both absent or both present; values must be finite |
| contact_id | contactId | Optional touch contact identifier |
| pressure | pressure | Optional finite numeric value; units are device-defined unless separately negotiated |
| button | button | Optional button identifier |
| metadata | metadata | Supplemental metadata; not an authority grant |

Unknown ABI versions and malformed required fields must be rejected before raw-event history is mutated.

## Device context

Each event is accompanied by current Coreless-negotiated device context: logical device ID, type, supported capabilities, designation state, and assignment generation. Interactive events must belong to the designated pointing device and match its advertised capabilities. Device-state telemetry may come from any discovered device, but is not sent to gesture interpretation by this adapter.

The context is supplied per submission. VIGIL does not persist it as proof that the same physical device will exist after a host migration.

## Raw vs. derived

The router appends a valid raw event before attempting optional interpretation. Interpreter availability, disablement, or a runtime failure must not discard or rewrite the raw event.

A derived event carries its own interpretation ID and kind, source device, timestamp, source sequence list, metadata/provenance, and optional confidence in the inclusive range 0–1. For an interpretation produced by the current event, provenance must include that triggering sequence and may not reference a future sequence. Derived records are kept separately from raw events.

If interpretation fails, the bridge records a diagnostic and returns no derived event, allowing the ordinary Coreless raw-input path to continue.

## Lifecycle and tests

1. Coreless validates transport and raw input.
2. Coreless supplies the event and current negotiated device context to the optional VIGIL adapter.
3. VIGIL validates the contract, preserves raw history locally for audit/replay, and optionally emits derived interpretation records.
4. Coreless remains responsible for delivery to GUI/applications and for any authorization-sensitive operation.

The deterministic JUnit tests cover ABI and coordinate validation, device mismatch, non-designated interactive input, capability mismatch, monotonic sequence ordering, derived-event provenance, interpreter unavailability/failure, and device-state handling.
