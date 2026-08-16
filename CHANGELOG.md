# Changelog

All notable Worldline changes are recorded here. The project follows semantic
versioning for declared stable contracts; experimental adapter APIs may change
before they are promoted.

## 1.0.0 - M12 Aero Reproduction

Status: GO for controlled real-scene capture and bounded spike reproduction.

- Added a test-only mapped runtime hook that creates a fixed-seed Aero MEGA
  world, forces the target chunks, fixes camera and velocity, and stops after a
  bounded 240-tick measurement window without modifying the Aero checkout.
- Captured two real Fabric Loader/StationAPI/LWJGL frame logs with dense Aero
  work and reproduced spikes attributed to expanded chunk-compilation work.
- Reused the M9 minimizer to reduce each stable-scene record window to one
  qualifying frame while retaining M11's neutral attribution boundary.
- Captured and hashed the generated save, and recorded that the current test
  fixture's custom MEGA BlockEntities do not survive a fresh save reload.
- Removed repository-wide line caps while retaining enforced per-file limits:
  250 product lines, 300 harness lines, and 150 smoke/adapter lines.

The frozen M12 invariant-report SHA-256 is
`a1d766e049b7382ae81e13e9b4ef4de3531c870869d7fe442e053f507af9169e`.

## 0.9.0 - M11 Aero Attribution

Status: GO for bounded work attribution and exact-candidate qualification.

- Added adapter-neutral frame-work comparison with explicit `LOGICAL_WORK`,
  `RUNTIME_STALL`, `MIXED`, and `INCONCLUSIVE` outcomes.
- Counted renderer work above the M10 Pbuffer without changing its frozen RGBA
  result and added an isolated Aero frame-log adapter.
- Pinned Aero Model Lib 3.0.0 at commit `436d65b`, ran all 222 core tests,
  built its StationAPI JAR and consumer, and loaded both test entrypoints in a
  real Fabric Loader/StationAPI client boot.
- Recorded the non-fatal startup diagnostic where showcase-block UV resolution
  occurs before atlas readiness; no historical-spike root cause is claimed.

The frozen M11 attribution SHA-256 is
`42e656576b70c53919761570abf016f93f76ddfbe49f3e40b79f2de0518eaecc`.

## 0.8.0 - M10 Native/Offscreen Render

Status: GO for the bounded render contract.

- Added a real 64 by 64 LWJGL/OpenGL Pbuffer lane isolated from the existing
  headless substitutions.
- Drew deterministic geometry through Minecraft's own `Tessellator`, verified
  exact pixel colors and coverage, and hashed the complete RGBA framebuffer.
- Added two mapped and two official-JAR processes with repetition, provenance,
  cross-boundary equality, and frozen-output checks.
- Investigated the original Aero target and recorded `artifact-absent` plus
  runtime compatibility `NOT_RUN`; no compatibility result was inferred.

The frozen M10 framebuffer SHA-256 is
`3f7da2d7ed9eeeff4c1ac7ad3767c82a5cb95b066cdb28bd3788e0cbcd3141ff`.

## 0.7.0 - M9 Automatic Scenario Minimization

Status: GO.

- Added canonical checksum-protected `.wlscenario` artifacts with bounded,
  ordered, adapter-neutral steps.
- Added exact first-divergence fingerprints and a deterministic delta debugger
  with cached evaluations, explicit budgets, and final one-step verification.
- Added CLI scenario creation/inspection with create-new output semantics.
- Added repeated real-runtime minimization across the two M8 mod versions,
  reducing nine noisy steps to a proven one-minimal three-step reproducer.

The frozen M9 minimization-report SHA-256 is
`706ff2a6fbeb2de5049749a573de95ba75ff43229326e7fd27a20aaf75b39a69`.

## 0.6.0 - M8 Differential Mod Testing

Status: GO.

- Added canonical `.wlmtest` results that bind mod identity, version, entrypoint,
  whole-JAR SHA-256, runtime/API declarations, and a canonical `v2` trace.
- Added stable mod-test comparison metadata and reused M6 first-divergence
  semantics without introducing a second trace comparison implementation.
- Extended the CLI and launcher with non-overwriting `mod test record` and
  equality/divergence-aware `mod test diff` commands.
- Added deterministic JAR packaging and repeated controlled-client evidence for
  a baseline and two versions of one mod, plus corrupt-result rejection.

The frozen M8 evidence-report SHA-256 is
`b08aa9f46b2d8522e6b8ac991553b2b6f946a63190d5956e59cbf6d544eb8938`.

## 0.5.0 - M7 General Mod Loading

Status: GO.

- Added a game-independent mod package module with strict canonical descriptor
  parsing, bounded JAR inspection, SHA-256 provenance, and explicit runtime/API
  compatibility results.
- Added descriptor-selected isolated entrypoint loading with type and code-origin
  checks; compatible code is never initialized before metadata acceptance.
- Extended the neutral CLI and repository launcher with `mod inspect` and stable
  compatible, incompatible, invalid-input, and usage exit codes.
- Added real controlled-client evidence for two independently selected mods and
  fail-closed runtime, API, entrypoint-type, malformed, and missing-descriptor cases.

The frozen M7 compatibility-report SHA-256 is
`bd13989879dba605a0cf790312c24a0f6947e87fb0b4d3ecd6f8cb265cbfb537`.

## 0.4.0 - M6 Trace Explorer

Status: GO.

- Added strict parsing and immutable models for schema-bearing canonical `v2`
  state traces.
- Added stable tabular rendering and structural first-divergence analysis for
  seed, schema, record labels/count, and individual field values.
- Extended the CLI and repository launcher with runtime-independent `trace
  show` and `trace diff` commands and explicit equality/divergence exit codes.
- Added fresh mapped/official trace equality, injected field divergence,
  reverse comparison, and malformed-schema rejection evidence.

The frozen M6 divergence-report SHA-256 is
`7eb4f707427c4e58ab3e481cc61f5801518325d5bbdfe045828325ab5ed2ea06`.

## 0.3.0 - M5 Reproduction Bundle

Status: GO.

- Added canonical `ReproductionBundle`, `ReplayProvider`, and `ReplayReport`
  contracts in an independently compiled module.
- Added a neutral replay CLI and a repository launcher that verifies local
  runtime inputs before starting the controlled b1.7.3 provider.
- Bundles embed the durable M4 snapshot while declaring the exact Worldline,
  runtime, official-client hash, and RetroMCP revision required for replay.
- Added two-process deterministic packing, copied-path CLI replay, official-JAR
  state comparison, and negative corruption/runtime/dependency evidence.

The frozen M5 bundle SHA-256 is
`840dca117939412dbba24594a1091c44d4b312b1e9700cec7aab7f47e0cc0181`.

## 0.2.0 - M4 Durable Snapshot

Status: GO.

- Added neutral `SnapshotMinecraftRuntime` and immutable, bounded
  `RuntimeSnapshot` contracts.
- Promoted the b1.7.3 replay checkpoint to a versioned canonical UTF-8 format
  with a body checksum and a frozen full-document SHA-256.
- Added strict parsing for runtime/version identity, field order, event count,
  numeric ranges, relative logical world sources, UTF-8, checksum, and exact
  canonical round-trip.
- Added cross-process capture and restore evidence, direct official-client
  state comparison, and executable corruption rejection.

The frozen M4 snapshot SHA-256 is
`a6e6589f9fdac1e40170f7a3b7fca7fc06b643b20a86249a464f9b2ab5b53bd2`.

## 0.1.0 - M3 Domain API

Status: GO.

- Added the opt-in `AutomatedMinecraftRuntime` without changing the v0.0.1
  lifecycle contract.
- Added neutral immutable block and position values plus stable world, entity,
  and local-player interfaces.
- Added lifecycle-guarded b1.7.3 implementations for world time, block
  read/write, active-entity snapshots, player state, teleportation, and hotbar
  selection.
- Added machine-verified mappings for every M3 field and method.
- Added a four-process differential oracle compiled independently against the
  official client JAR.

The frozen M3 signature is
`d38186377edc68f8080e568ffaba6559c4b3980fcf2a5311aac1b6ec7ebcc13c`.

## 0.0.1 - Controlled Tick

Status: GO.

Stable milestone contract:

- freeze and hash the official Minecraft Beta 1.7.3 client artifact;
- pin and verify the RetroMCP toolchain;
- reconstruct and compile the mapped client locally;
- boot the real client object graph without a native window;
- load a deterministic in-memory world;
- advance exactly one externally requested `Minecraft.runTick()`;
- match an independent oracle compiled against the official client JAR in two
  fresh subject and two fresh oracle JVMs.

The frozen first-tick signature is
`ac13115a73408c85eb80b931dc3004b4fd66b26a5512e8d4fb036eebf70ae780`.

Release qualification includes two cold RetroMCP reconstructions. Decompiled
`World` source was not byte-stable, so v0.0.1 explicitly guarantees frozen
inputs and oracle-verified observable behavior, not byte-identical decompiler
output.

Experimental capabilities shipped alongside the milestone include a reusable
b1.7.3 adapter, 16-tick state traces, deterministic external boundaries,
replay-backed checkpoints, hypothesis branches, semantic inventory GUI
actions, and isolated mod-JAR loading. These do not enlarge the stable v0.0.1
contract.
