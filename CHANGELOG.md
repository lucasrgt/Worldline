# Changelog

All notable Worldline changes are recorded here. The project follows semantic
versioning for declared stable contracts; experimental adapter APIs may change
before they are promoted.

## Unreleased - M16 Time-Travel Debug

Status: GO.

- Added `worldline debug <scenario.wlscenario> <seed>`: an interactive,
  scriptable session over one public-grammar scenario with forward steps,
  exact reverse jumps and absolute gotos (deterministic prefix replay),
  recorded-observation dumps, and per-field watchpoints.
- Added the neutral `ScenarioTimeTravel.prefix` contract implemented by the
  b1.7.3 scenario runner; sessions emit stable `WORLDLINE_DEBUG_*` lines so
  scripted transcripts are frozen evidence.
- Evidence: thirteen-command scripted transcript covering triggers, stale
  observations, clamped goto, unknown commands, and unwatch; frozen SHA-256
  in `smokes/m16-debug/smoke.properties`.

## Unreleased - Pre-push Verification Gate

Status: GO.

- Added a versioned `tools/hooks/pre-push` hook that runs the canonical gate
  before every push, with `WORLDLINE_PREPUSH_SMOKE=1` demanding the full
  evidence suite.
- Activated per clone via `git config core.hooksPath tools/hooks`; documented
  in the engineering guide and README.

## Unreleased - M15 Differential Fuzzer

Status: GO.

- Added the deterministic `worldline fuzz <out-dir> <seed> <cases> <steps>
  [left.jar] [right.jar]` campaign command over public-grammar scenarios.
- Added `worldline.fuzz` with bounded scenario planning, named subjects,
  pairwise divergence search, vanilla self-checks for nondeterminism, and
  automatic minimization of every divergence into a `.wlscenario` reproducer.
- Added the canonical checksum-protected `WORLDLINE-FUZZ/1` report with
  embedded scenario artifacts and stable subject provenance.
- Added adapter-side fail-closed validation for unregistered block ids in
  scenario block writes.
- Evidence: two-mod campaign finds and shrinks the first divergence inside the
  budget while a vanilla-only campaign stays clean; frozen SHA-256 in
  `smokes/m15-fuzz/smoke.properties`.

## Unreleased - M11 Mod API v2

Status: GO.

- Expanded `B173ModContext` with stable M3 `world()` and `player()` handles
  and deterministic scheduled actions (`at(tick, action)`) drained before each
  tick's mod callbacks.
- Added additive lifecycle hooks to `B173Mod`: `onLoad` runs on installation
  into a loaded world; `onDispose` runs in reverse install order at close.
- Added a bounded semantic spawn registry (`GameWorld.spawn`), entity removal
  (`GameWorld.remove`), container reads (`GameWorld.itemsAt`), and vanilla
  merge semantics inventory mutation (`GamePlayer.give`) as opt-in overrides
  over fail-closed defaults.
- Added ordered multi-mod installation through `B173Runtime.installMods`.
- Evidence: two-process deterministic lifecycle smoke with rejection matrix;
  frozen SHA-256 in `smokes/m11-mod-api/smoke.properties`.

## Unreleased - M12 Attested Mod Test Run

Status: GO.

- Added the one-command `worldline mod test run <mod.jar> <seed> <ticks>
  <result>` flow: inspect, load, boot, install, tick, record, write.
- Added `WORLDLINE-MOD-TEST/2` results binding `execution=controlled-runtime`,
  `seed`, and `ticks`; v1 parsing and recording remain unchanged.
- Added the neutral `ModTestRunner` provider contract with reflective CLI
  binding via `worldline.modtest.provider`.
- Evidence: end-to-end launcher smoke with byte-identical executed results,
  EQUAL diff, and corruption rejection; frozen SHA-256 in
  `smokes/m12-mod-run/smoke.properties`.

## Unreleased - M13 Multi-Mod Graph

Status: GO.

- Added descriptor format 2 with an optional canonical `requires=` field of
  `id` / `id>=x.y.z` dependency tokens; format 1 stays valid and dependency
  free.
- Added `ModGraph.order` with topological resolution, lexicographic
  tie-breaking, input-order independence, and fail-closed rejection of
  duplicates, missing dependencies, unmet minimums, self dependencies, and
  cycles.
- Extended `mod inspect` output with the resolved `requires=` list.
- Evidence: neutral ordering smoke frozen in
  `smokes/m13-mod-graph/smoke.properties`.

## Unreleased - M14 Public Scenario DSL

Status: GO.

- Added `worldline-scenario-dsl/1`: strict grammar and canonical rendering for
  `tick[:n]`, `reseed:<long>`, `tap:<key>`, `observe:<label>`, and
  `block:x,y,z:id[:meta]` steps with bounded values.
- Added the neutral `ScenarioRunner` contract and reflective CLI binding so
  scenarios execute against the controlled runtime:
  `worldline scenario validate|run`.
- DSL scenarios remain ordinary M9 artifacts; minimization applies unchanged.
- Evidence: authoring, validation, rejection, and deterministic execution
  smoke frozen in `smokes/m14-scenario-dsl/smoke.properties`.

## Unreleased - Butter HostUi screens

- `B173Gui` promotes Butter screens that implement `butter.testing.HostUi`.
- Binding is reflective so Worldline product modules still do not import Butter.
- `B173Gui.open` displays a mapped `GuiScreen` so the laboratory can open Butter.
- `B173Gui.putMain` seeds `mainInventory` for Butter slot observation.
- Vanilla inventory `GameUi` behavior is unchanged. Other vanilla screens still
  fail closed.

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
