# VIGIL

**Visual Intelligence & Geographic Information Layer**

VIGIL is an AI-powered environmental awareness and security platform designed to combine information from cameras, location and orientation sensors, maps, and other authorized data sources into an evidence-grounded spatial representation of an environment.

The project is currently in the architecture and technical-design phase. No application implementation is committed yet.

## Project Documentation

The current architecture baseline is defined by the Architecture Specification and Architecture Contract. The Spatial World Model Technical Design defines the current detailed spatial-state design.

Start with `docs/architecture/VIGIL-ARCHITECTURE-SPEC.md`.

Current baseline:

- Architecture Specification: version 0.3
- Architecture Contract: version 0.3
- Spatial World Model Technical Design: version 0.2

## Design Principle

VIGIL separates observation from interpretation. Authorized sources produce observations; perception derives detections; tracking maintains temporal continuity; spatial and temporal fusion combines compatible evidence; the Spatial World Model maintains authoritative supported world state; deterministic spatial services calculate geometry and relationships; and AI reasons over structured information while preserving applicable confidence, uncertainty, validity, freshness, and provenance.

## Repository Status

The architecture baseline is established before application implementation. Applicable contracts and technical designs shall be reconciled and verified before implementation relies on their requirements.
