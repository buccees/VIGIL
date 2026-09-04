# VIGIL Spatial / Temporal Fusion Contract

**Project:** VIGIL  
**Document:** Spatial / Temporal Fusion Contract  
**Version:** 0.1  
**Status:** Proposed for implementation review  
**Parent:** VIGIL Architecture Contract / Spatial World Model Technical Design  
**Audience:** Developers, reviewers, maintainers, and future contributors

## 1. Purpose

This contract defines the normative boundary for Spatial / Temporal Fusion within VIGIL.

Fusion converts compatible observations, detections, tracks, and other authorized evidence into spatially and temporally aligned estimates that may be submitted to the Spatial World Model.

Fusion SHALL remain a derived-information function. It SHALL NOT become an independent authority over authoritative World Model state, physical identity, security authorization, or consequential physical action.

## 2. Contract Authority and Precedence

This contract is subordinate to the VIGIL Architecture Contract.

The Spatial World Model Technical Design defines the receiving world-state boundary and data-model requirements applicable to fusion output.

The applicable Track/World Model contract, when established, SHALL define detailed track-to-world-state update behavior at that boundary. Until that contract is established, this contract SHALL NOT be interpreted as creating a competing Track → World Model authority.

Implementation details MAY vary provided they preserve the semantic requirements of this contract and all higher-level contracts.

## 3. Normative Language

The terms **MUST**, **MUST NOT**, **SHALL**, **SHALL NOT**, **SHOULD**, and **MAY** are normative.

- **MUST / SHALL** defines a required behavior.
- **MUST NOT / SHALL NOT** defines a prohibited behavior.
- **SHOULD** defines a preferred behavior that may be deviated from only for a documented reason.
- **MAY** defines an allowed but optional capability.

## 4. Fusion Boundary

The canonical information lifecycle SHALL remain:

`Observation → Detection / Perception → Track Continuity → Spatial / Temporal Fusion → Spatial World Model`

Fusion SHALL consume validated inputs from upstream components and SHALL produce derived estimates or explicitly unresolved results.

Fusion SHALL NOT bypass Track Continuity when a track is required by the applicable processing path.

Fusion SHALL NOT directly mutate authoritative Spatial World Model state.

All fusion-derived authoritative state SHALL cross into the Spatial World Model through the controlled `WorldModelUpdater` boundary.

## 5. Inputs

Fusion MAY consume, where authorized and applicable:

- observations;
- detections;
- tracks;
- sensor metadata;
- sensor health and calibration state;
- coordinate transforms;
- source timing information;
- map or environmental evidence;
- previously fused estimates; and
- other explicitly authorized evidence.

Each fusion input SHALL retain sufficient identity, timing, provenance, quality, confidence, and uncertainty information to evaluate whether it is suitable for fusion.

An input SHALL NOT be treated as authoritative merely because it originates from a trusted software component or because it has a stable internal identifier.

## 6. Input Validation

Fusion SHALL validate applicable input conditions before producing an authoritative-eligible result.

Validation SHALL include, where applicable:

1. coordinate-frame validity;
2. transform validity for the relevant time and calibration state;
3. unit compatibility;
4. timestamp validity and ordering semantics;
5. source freshness;
6. sensor health and calibration state;
7. input validity state;
8. uncertainty availability or explicit absence;
9. provenance availability; and
10. compatibility with the active fusion policy.

Unknown, invalid, incompatible, or materially insufficient inputs SHALL NOT be silently converted into valid values.

## 7. Temporal Alignment

Fusion SHALL distinguish event time from ingestion and processing time.

Fusion SHALL support delayed and out-of-order inputs where the applicable processing policy permits their use.

Fusion SHALL NOT assume that ingestion order represents physical-event order.

When temporal alignment depends on uncertain timestamps, the resulting temporal uncertainty SHALL remain represented in the fusion result or SHALL cause the result to be explicitly qualified as unresolved when the uncertainty prevents a reliable result.

Older evidence SHALL NOT silently overwrite a newer authoritative current-state estimate merely because it was processed later.

## 8. Spatial Alignment

Fusion SHALL perform spatial calculations only after validating the coordinate frames and required transforms.

A transform SHALL identify, directly or through a resolvable reference:

- source frame;
- destination frame;
- applicable time or validity interval;
- calibration state where relevant; and
- transform validity.

Unknown, invalid, incompatible, or stale transforms SHALL NOT be silently applied.

A spatial result dependent on an invalid calibration or transform SHALL be rejected, quarantined, or explicitly marked invalid/degraded according to the applicable input/state contract.

## 9. Evidence Compatibility

Fusion SHALL combine only evidence that is compatible under the active fusion policy.

Compatibility MAY depend on:

- spatial frame;
- temporal alignment;
- sensor/source state;
- measurement type;
- units;
- uncertainty representation;
- freshness;
- provenance;
- expected measurement characteristics; and
- known correlation or dependency between evidence sources.

Evidence SHALL NOT be combined solely because the evidence references the same nominal object identifier.

When required compatibility cannot be established, the evidence SHALL remain uncombined or the resulting estimate SHALL be explicitly qualified as unresolved.

## 10. Identity and Association

Fusion SHALL preserve the distinction:

`Detection ID ≠ Track ID ≠ World Entity ID`

A fusion association key, including `FusedEstimate.associationId` where used, SHALL be treated as a deterministic association mechanism and SHALL NOT by itself establish authoritative physical identity.

Fusion SHALL NOT manufacture a World Entity ID when the available evidence does not support the required association.

Where identity evidence is insufficient or materially conflicting, fusion SHALL preserve the ambiguity rather than silently selecting an identity.

## 11. Confidence and Uncertainty

Fusion SHALL preserve confidence and uncertainty as separate dimensions.

Fusion SHALL NOT convert uncertainty into confidence, or confidence into an estimate of numerical error.

A fused estimate SHALL retain uncertainty appropriate to the result, including covariance, error bounds, intervals, or another typed representation when available.

If a reliable fused uncertainty cannot be established, the uncertainty SHALL remain explicitly unknown rather than being replaced by unsupported precision.

## 12. Conflict and Disagreement

Fusion SHALL preserve material disagreement between contributing evidence sources.

When evidence materially disagrees and the active fusion policy does not provide a justified resolution, fusion SHALL return an explicitly unqualified or unresolved result rather than silently selecting one source as authoritative.

A source SHALL NOT become authoritative solely because it has higher confidence, newer processing time, a preferred presentation priority, or a convenient implementation order unless the active fusion policy explicitly establishes that rule.

When a documented fusion policy resolves a conflict, the result SHALL retain sufficient provenance and state information to identify that a conflict existed and how it was resolved, where technically applicable.

## 13. Provenance

Every fusion result eligible for downstream authoritative use SHALL retain traceable provenance to its contributing evidence.

The provenance chain SHALL remain resolvable to the applicable upstream observations or authorized source records, subject to retention and security policy.

Fusion SHALL NOT discard provenance merely because multiple inputs have been combined into one estimate.

## 14. Freshness and Validity

Fusion SHALL evaluate the freshness and validity of contributing evidence when those properties materially affect the result.

A fused estimate SHALL NOT be presented as current when the evidence required to support its current validity is stale or invalid.

A result MAY remain stored for historical or diagnostic purposes after becoming stale or invalid, but its current-state validity SHALL remain explicit.

## 15. Output Contract

A fusion result SHALL contain, where applicable:

- result identity;
- association information;
- estimated spatial state;
- temporal reference;
- confidence;
- uncertainty;
- validity;
- freshness;
- provenance;
- contributing evidence references; and
- conflict or qualification state when material.

Conceptual result structure:

```text
FusedEstimate
├── result_id
├── association_id
├── spatial_state
├── temporal_reference
├── confidence
├── uncertainty
├── validity
├── freshness
├── provenance
├── evidence_references
└── qualification
```

The exact programming-language representation is implementation-defined unless established by a more specific contract.

## 16. World Model Update Boundary

Fusion SHALL produce a derived result; it SHALL NOT commit that result directly to authoritative World Model state.

The controlled `WorldModelUpdater` SHALL validate fusion output before authoritative state mutation.

The update path SHALL preserve:

- entity identity;
- provenance;
- confidence;
- uncertainty;
- freshness;
- validity; and
- material qualification or conflict state.

A fusion implementation SHALL NOT provide an alternate write path that allows fusion output to bypass the controlled update boundary.

## 17. Determinism and Reproducibility

The deterministic portion of fusion SHALL be testable without live hardware.

Given equivalent inputs, configuration, fusion policy, and execution-relevant versions, deterministic fusion SHALL produce reproducible results within the documented numerical tolerance.

Randomized or model-based fusion components MAY be used when explicitly authorized, but their model/version, configuration, and relevant uncertainty SHALL remain traceable.

Replay of recorded observations SHALL exercise the same semantic fusion boundary used by live processing where the implementation supports equivalent processing.

## 18. Failure and Degraded States

Fusion SHALL fail closed with respect to unsupported certainty.

When required evidence, transforms, timing, calibration, or compatibility information is unavailable, fusion SHALL produce one of the following according to the applicable policy:

- no fusion result;
- a result explicitly marked invalid or degraded; or
- an explicitly unresolved result.

Fusion SHALL NOT fabricate a position, timestamp, identity, confidence, uncertainty, or precision to complete a processing step.

A failure in one evidence path SHALL NOT silently erase valid independent evidence unless the active fusion policy requires that behavior and records the applicable state.

## 19. Security and Authorization

Fusion SHALL consume only evidence and metadata for which the calling component is authorized.

Fusion SHALL NOT expand authorization based on an evidence identifier, track identifier, association identifier, or world-entity identifier.

Authentication and authorization SHALL remain distinct concerns.

Fusion SHALL preserve applicable data classification, retention, provenance, and audit requirements across its processing boundary.

## 20. AI and Model-Based Processing

AI or statistical models MAY assist fusion where authorized by the applicable architecture and technical contracts.

A model output SHALL remain a derived result and SHALL NOT acquire authoritative status merely because a model produced it.

Model-based processing SHALL preserve, where applicable:

- model/version identity;
- input evidence references;
- confidence;
- uncertainty;
- validity;
- provenance; and
- qualification or unresolved state.

AI SHALL NOT silently rewrite authoritative World Model state through a fusion path.

## 21. Non-Goals

This contract does not define:

- authoritative track lifecycle states;
- the complete Spatial World Model data schema;
- application-specific presentation or priority behavior;
- human conversational behavior;
- weapon-control logic;
- actuator control; or
- autonomous consequential physical action.

Those concerns remain governed by their applicable higher-level or component-specific contracts.

## 22. Verification Requirements

An implementation SHALL provide automated verification for the normative behaviors in this contract, including as applicable:

1. coordinate-frame validation;
2. transform validity;
3. temporal alignment;
4. delayed and out-of-order evidence;
5. confidence and uncertainty separation;
6. uncertainty propagation;
7. stale and invalid input handling;
8. material evidence disagreement;
9. provenance retention;
10. identity-domain separation;
11. controlled WorldModelUpdater enforcement;
12. deterministic replay;
13. authorization boundaries; and
14. degraded/unresolved result behavior.

Tests SHALL demonstrate that invalid or unsupported information cannot silently become authoritative certainty through fusion.

## 23. Implementation Readiness

This contract is **Proposed for implementation review**.

Implementation relying on this contract SHALL NOT be considered production-ready until:

- the contract has been reconciled with the Architecture Contract;
- the Spatial World Model technical design accepts the defined fusion boundary;
- any applicable Track/World Model contract is reconciled when established;
- open design decisions that materially affect fusion semantics are finalized; and
- the verification requirements in this contract are implemented and passing.

Until those conditions are satisfied, implementations MAY proceed only to the extent that they do not depend on unresolved contractual behavior.

## 24. Change Control

Changes to this contract SHALL identify their effect on:

- the Architecture Contract;
- the Spatial World Model technical design;
- the Track Continuity boundary;
- the WorldModelUpdater boundary;
- identity semantics;
- time and freshness semantics;
- confidence and uncertainty semantics;
- provenance;
- security and authorization; and
- verification requirements.

A semantic conflict SHALL be resolved by explicit contract revision and reconciliation before dependent implementation relies on the changed behavior.
