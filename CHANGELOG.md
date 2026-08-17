# Changelog

All notable Worldline changes are recorded here. The project follows semantic
versioning for declared stable contracts; experimental adapter APIs may change
before they are promoted.

## 1.10.0 - M22 Multiplayer Wire Harness

Status: GO for localhost protocol-14 login and player-presence control.

- Added neutral `MultiplayerSession`, `MultiplayerState`, and
  `MultiplayerServerRuntime` contracts.
- Added an original minimal b1.7.3 protocol-14 client for the native offline
  handshake and login response.
- Repeated two fresh scenarios where the official server lists exactly one
  Worldline client, then returns to an empty list after socket disconnect.
- Kept the official graphical client, movement, full play packets, and tick
  determinism as explicit later milestones.

The frozen M22 multiplayer SHA-256 is
`723f96819bd972ec5f2a4d932251840099f2d6472edf590c4386641a7d7e08f9`.

## 1.9.0 - M21 Dedicated Server Control

Status: GO for neutral command, save, and persisted-state control.

- Added `DedicatedServerRuntime`, `ServerLifecycle`, and immutable `ServerState`
  as the first game-neutral dedicated-server surface.
- Added a b1.7.3 process adapter that uses native console commands and reads
  persisted `level.dat` NBT without patching or decompiling the server.
- Booted two fresh official servers, set time to 6000, forced saves, observed
  the persisted time, and required clean native shutdown.
- Kept tick stepping, client login, packets, and multiplayer determinism as
  explicit later milestones.

The frozen M21 control SHA-256 is
`87035c21599513c04b6fe5b5622232a485a7f5c5e52778ecf11428ef671b4d4f`.

## 1.8.0 - M20 Official Server Bootstrap

Status: GO for official dedicated-server identity and lifecycle control.

- Added a frozen public descriptor for the unmodified Beta 1.7.3 dedicated
  server while keeping the proprietary JAR under ignored `local/artifacts/`.
- Added an HTTPS artifact acquisition tool that validates byte length, SHA-1,
  and SHA-256 before installing either the client or server JAR.
- Started two fresh localhost-only official servers, reached native readiness,
  issued `stop`, observed save/shutdown, and required clean exits.
- Kept server instrumentation, client connection, and multiplayer determinism
  as explicit later milestones.

The frozen M20 lifecycle SHA-256 is
`7d1edb19b978300465878cfade247ec0db7db37b9a5fbcfd9a595566bfb06b60`.

## 1.7.0 - M19 Forced Autosave

Status: GO for the default-off one-chunk save cap; the historical random spike
remains a non-claim.

- Added a look/jump/spin tower path and marked 60 loaded chunks dirty before
  native 40-tick autosaves.
- Compared vanilla's 24-chunk non-forced batch, an opt-in one-chunk cap, and
  the existing save-cancelled control on restored copies of one dense save.
- Proved that the cap keeps saves active while reducing the observed worst
  save; exact timings remain machine-local observations.
- Kept the cap default-off, the adaptive scheduler lab-only, and the M16 visual
  threshold unchanged.

The frozen M19 invariant-report SHA-256 is
`9ca8c14f03615b25891a8468a946bbbe7b889d8de747a8d0e03cb73665970bb1`.

## 1.6.0 - M18 Save Attribution

Status: GO for paired save-path attribution; the historical random spike
remains a non-claim.

- Parameterized the Aero capture skip-saves flag so M12-M17 keep cancelling
  non-forced saves while M18 can turn them back on.
- Injected one non-forced world save at a known tick from the Worldline
  capture mixin, without editing the pinned Aero checkout.
- Proved the skipped dense twin cancels that save and the live twin records
  `worldSaveMs` on the same line as compile, GC, heap, and allocation
  counters.
- Left the adaptive scheduler lab-only NO-GO and kept M16's framebuffer
  threshold unchanged.

The frozen M18 invariant-report SHA-256 is
`855ae55bc5944ae98d3fb6b66fe6840fc7561d425ce620b9ba45a55720f6c7bd`.

## 1.5.0 - M17 Scheduler Hardening

Status: GO for the three-scenario qualification matrix; NO-GO for scheduler
promotion.

- Added stationary-empty, stationary-dense, and moving-dense comparisons of
  vanilla retries, Aero's old governor, and the visible-first adaptive policy.
- Proved one adaptive completion per frame and eventual global drainage without
  background starvation across the matrix.
- Confirmed that the old governor retains backlog, while the adaptive envelope
  remains non-preemptive and can overshoot on one expensive rebuild.
- Kept moving-window readiness and scenario-dependent timing observational
  after qualifying repetitions changed their comparative direction.
- Corrected M13's historical scene-pressure gate to require exercised chunk
  compilation without freezing a machine-dependent 10 ms timing crossing.
- Stabilized checkpoint entities, daylight, weather, camera, and interpolation;
  all three framebuffer pairs still exceed M16's strict pixel tolerance.
- Packaged a default-off evaluation profile marked `lab-only-no-go`; the pinned
  Aero checkout remains unchanged.

The frozen M17 invariant-report SHA-256 is
`fa008e18e53b8d63003196e91d2b554f4ce973e602a68df4c7a7dc77096f7456`.

## 1.4.0 - M16 Adaptive Chunks

Status: GO for the visible-first adaptive scheduler; corrected startup rendering
does not reproduce the original fixed-state pixel parity.

- Added visible-debt bands of 2/4/6/8 accepted rebuilds under a 12 ms rebuild
  envelope while preserving one explicit accepted/deferred call per frame.
- Closed the first-300-frame visible readiness gap relative to vanilla and
  reduced the release-gate run's worst frame from 735.2 ms to 218.6 ms.
- Added a frozen-tick framebuffer oracle that fixes camera/interpolation, drains
  global chunk work, and compares every baseline/candidate RGBA pixel against a
  64-pixel, 2-channel-level decision threshold. M17's overlay correction now
  records a threshold violation.
- Added canonical save snapshot/restore so independently ordered world
  generation cannot contaminate the scheduler differential.
- Hardened the legacy M15 gate to compare normalized visible readiness and
  queue drainage instead of incomparable absolute frustum counts, and to apply
  its geometry threshold to the fixed-camera comparable cohort.
- Preserved the pinned Aero revision and the repository's per-file-only source
  limits.

The frozen M16 invariant-report SHA-256 is
`f274b0970e16939ba56b8f8796360d54c5f7981168a1e52e9d85da95585eb26b`.

## 1.3.0 - M15 Explicit Chunk Contract

Status: GO for the explicit accepted/deferred boundary and readiness evidence;
the fixed two-rebuild policy remains experimental.

- Added adapter-owned `COMPLETE`, `ACCEPTED_DEFERRED`, and `STALLED_DEFERRED`
  outcomes, mapped to vanilla's Boolean only at the render caller.
- Proved one contract invocation per frame with two real accepted rebuilds,
  next-frame resumption, and no same-frame retries or stalled batches.
- Measured dirty age and visible built/clean state from the first world frame;
  comparative readiness is reported but no longer frozen across machines.
- Added an exact Tessellator vertex-stream oracle. Most common non-empty chunks
  match exactly while nonzero tick-dependent temporal differences are retained.
- Retained only per-file source limits and left the pinned Aero checkout clean.

The frozen M15 invariant-report SHA-256 is
`64f635a1ed85ce0d9d30b468937b7803a06418e783f6ae8643da69877d597ba1`.

## 1.2.0 - M14 Chunk Backlog

Status: GO for caller semantics, initial-backlog isolation, and the bounded
non-retry prototype; the policy remains experimental.

- Proved that the primary render caller passes `forced=false` and retries
  `compileChunks` while it returns `false`, until the frame deadline.
- Measured thousands of dirty builders after warmup and continuous rebuilds in
  mostly quiet frames, attributing stable-camera pressure primarily to the
  initial queue rather than continuous new dirtiness.
- Added a smoke-only policy that uses vanilla priority ordering, performs two
  real rebuilds in one call per frame, and returns `true` without a retry storm.
- Added strict chunk-probe parsing, same-input fresh-world comparison, and
  explicit terrain-latency and visual-equivalence non-claims.

The frozen M14 invariant-report SHA-256 is
`65f43a875d18e96066441cb308fed7089bab8414b087f4398c1555211f2bae6a`.

## 1.1.0 - M13 Aero Differential

Status: GO for persistence isolation and the bounded chunk-path differential.

- Distinguished global BlockEntities from real entity blocks and proved that
  the 576 real fixture blocks persist while excess phantom entries disappear.
- Added equal-control dense and Aero-disabled captures; both exercise the same
  substantial chunk-compilation path and exploratory runs spike in both, so a
  stable dense amplification is not claimed.
- Exercised the optional compile governor on the render path and rejected it after
  the always-active control produced a hot retry storm.
- Added a strict Aero diagnostics adapter and a four-mode executable gate while
  retaining only per-file source limits.

The frozen M13 invariant-report SHA-256 is
`1759de8beeeef257a4027fd79f590ec7a72d364729863d1cb5fe373741399e80`.

## 1.0.0 - M12 Aero Reproduction

Status: GO for controlled real-scene capture and bounded spike reproduction.

- Added a test-only mapped runtime hook that creates a fixed-seed Aero MEGA
  world, forces the target chunks, fixes camera and velocity, and stops after a
  bounded 240-tick measurement window without modifying the Aero checkout.
- Captured two real Fabric Loader/StationAPI/LWJGL frame logs with dense Aero
  work and reproduced spikes localized to the chunk-compilation stage.
- Reused the M9 minimizer to reduce each stable-scene record window to one
  qualifying frame while retaining M11's neutral attribution boundary.
- Captured and hashed the generated save; M13 later distinguished its
  persistent real entity blocks from non-persistent phantom global entries.
- Removed repository-wide line caps while retaining enforced per-file limits:
  250 product lines, 300 harness lines, and 150 smoke/adapter lines.

M13 corrected the M12 oracle so stable single-call slow compiles are not
misreported as expanded logical-work counts. The corrected M12 invariant SHA-256 is
`804915ae89a1adef9f350adc020ed8a77986b2d3d4c1d84205009a4382ed051c`.

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

## Unreleased - Optimization Metadata SDK

- Added dependency-free, source-retained `OptimizationRef` metadata in its own
  optional module; it injects no runtime behavior or bytecode.
- Added a properties-backed record schema for status, defaults, behavior delta,
  risks, rollback, source symbols, and evidence.
- Added fail-closed canonical checks for unknown IDs, incomplete records,
  unevidenced decisions, unsafe defaults, and annotation/symbol tracking drift.
- Made repository ownership explicit: Worldline contains only the neutral SDK
  and its own records; mods own implementation-specific catalogs and Worldline
  evidence refers to their stable IDs.
- Added isolated positive and negative checker fixtures without coupling mod
  sources or implementation knowledge to Worldline.

## Unreleased - M2 Controlled Runtime

Status: GO.

- Promoted virtual clock, programmable input, RNG reseed, filesystem
  journal/failure injection, offline network, tick scheduler, and timer-thread
  supervision from experimental evidence to a stable milestone.
- The public product version remains 0.7.0 / M9. M2 does not add
  `worldline-api` types; boundary controls stay on the b1.7.3 adapter.
- Frozen evidence is the existing four-process 16-tick client state signature
  `e8cdeba39a44b772a70c48c0acd9ae3983f3d95a8c10c545df5d66fb953db554`.

## Unreleased - Semantic Mappings

Status: GO.

- Added `SemanticMapping` so a b1.7.3 symbol can carry role, category,
  reads/writes, dependencies, evidence, an optional official client alias,
  and confidence.
- Added `worldline-semantics` with a fail-closed catalog of the 24 control
  categories and 196 required roles, including both `symbols.map` files,
  adapter/oracle fields, item/recipe/domain surfaces, and the native
  autosave, chunk-save, and compile-chunk symbols.
- Added `AdapterManifest` so Worldline adapters declare catalog sites without
  placing Aero or other external types in `SemanticCatalog.standard()`.
- Added a fail-closed coverage gate so every named `symbols.map` symbol has
  a catalog role, plus `SemanticGraph` over static read/write/dep tokens.
- Added CLI `semantics show|graph|category|role` inspection without loading
  Minecraft.
- Trace CLI diffs print a catalog `role=` alias for known fields. Scenario
  minimization tries disposable lab/noise steps first. Frozen M6/M8 CLI-report
  hashes now include those role lines; M9 evidence uses 21 evaluator calls.
- Diverged conservation fields also print `invariant=<rule>` after the M6
  document. `block65` names `block-conservation`. Frozen M8/M9 hashes include
  that line.

## Unreleased - Invariant Engine

Status: GO.

- Added `ItemCensus` and `InvariantViolation` so observed item totals are
  immutable API values.
- Added `worldline-invariants` with `InvariantEngine` and `ItemConservation`.
  The first census is the baseline; later gains fail closed, losses do not.
- Added `GamePlayer.items()` and opt-in `watch` so each controlled tick samples
  player and world totals without opening a screen.
- World census includes dropped items and loaded container inventories.
- Item conservation is now consecutive and recipe-aware. A gain holds when a
  `RecipeBook` can account for it; unexplained creation still fails closed.
- Crafting container leftovers (empty buckets from milk) are folded into
  recipe outputs so cake no longer looks like item creation.
- `GameWorld.blocks()` and block-drop recipes explain harvest gains (stone
  to cobble, log to log), including sampled random quantities.
- `EntityCensus`, `CauseDrop`, and `DropBook` explain mob death, chicken
  eggs, and caught fish. Newly loaded chunk items are imports, not creation.
- Added `TimeMonotonic` and `InvariantEngine.standard` so world time cannot
  move backward.
- Added `EntitySpawn`, `BlockConservation`, `HealthConservation`, and
  `DurabilityConservation`. `standard(runtime)` also loads block transforms,
  fluid/fire/plant presence, food heal amounts, and host spawn rules.
- The controlled-client cycle watches `standard(runtime)` for 16 live ticks.
  Falling sand, thrown items, lit-block swaps, and `GameWorld.peaceful()`
  complete the world-tick cause book.
- Removed total line budgets for harness, smoke, and adapter. Only per-file
  ceilings remain.

## Unreleased - Game UI Tree

Status: GO.

- Added `UiMinecraftRuntime`, `GameUi`, and immutable `GameUiNode` values for a
  semantic inventory tree (`screen`, `slot`, `node`, `click`).
- Added a four-process official-JAR differential that opens, inspects, clicks,
  and closes the inventory screen without mapped types in the caller.
- Added `GameUiSpec` so Aero Machine Maker `guiComponents` and a live `GameUi`
  tree share role/name/index without pixels.
- Added a Flutter-inspired `Ui.screen/row/slot` declaration that flattens to
  the same spec. Layout widgets do not become DOM nodes.

The frozen GUI-tree SHA-256 is
`ab13a631ed766de32f2947fae1a6e0a86d9b6cde3cbc7e1557ff76f76ccc60cf`.

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
