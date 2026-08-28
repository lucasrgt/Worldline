# Worldline changelog — Unreleased

## Unreleased - Functional Census Physical Envelopes

Status: GO.

- Added public, data-driven physical-envelope families for remaining opaque cubes, common cubes,
  and special-collision blocks. The official Beta 1.7.3 cycles jointly prove state domain,
  collision, light behavior, and fresh-login persistence without per-block milestone inflation.
- Corrected the remaining opaque-cube milestone identity so its objective, smoke, documentation,
  semantic map, and evidence rows describe the capability actually executed.
- Kept snow-block tick policy partial: the bounded dark-survival fixture does not prove the
  independently causal high-light random-tick melt boundary.
- The audited Functional Census now contains 587 of 1,056 verified claims (55.59%), leaving 457
  claims to the 98.8% target.
- Shared physical-placement support changes are carried through reviewed refactor-equivalent
  proof migrations only when their tracked official observation hashes remain unchanged.
- Composite and telemetry pin migrations retain their prior official evidence envelopes while
  binding the new shared TestKit fingerprint; exact wave receipts remain `executed`.
- Repository-schema, neighbor, support-face, bounded-drop, and downstream TestKit migrations are
  regenerated as ordered proof transitions; unchanged pins retain their immediate predecessor
  identity.
- Added a singular tile-utility physical-envelope package for dispenser, note block, mob spawner,
  chest, furnace, and jukebox. It coordinates five state rows and six collision/light rows while
  explicitly withholding occupied-jukebox metadata until a record-action loadout is proved.

## Unreleased - Ground-Cover Lifecycle Family

Status: GO.

- Public `BlockLifecycleFamilyCycle` family `ground-cover` proves tall-grass on dirt, dead-bush
  on sand, and age-zero wheat from seeds on protocol-provisioned farmland.
- Official Beta 1.7.3 drop identity is bounded: seeds `295` 0..1 from tall grass, sticks `280`
  0..2 from dead bush, and seeds `295` 0..3 from age-zero wheat. Farmland is admin setup only.

## Unreleased - Integration Hygiene and Anti-Slop Audit

Status: developer tooling; not a vanilla SET GO.

- Added the `b1-7-3-anti-slop` repository skill, its evidence-backed review
  references, and a fail-closed side-safety audit for common and server roots.
- GUI smoke cleanup now removes an empty per-arm worktree parent after both
  registered Aero worktrees are gone, while retaining the existing path and
  registration safety checks.
- Runtime Fabric self-tests require parent-cleanup coverage across all 37 GUI
  smoke cycles.
- The analysis-dependent seed-atlas runner now has a dedicated optional server
  adapter source set, keeping ordinary official-server smoke compilation
  API-only.

## Unreleased - WorldlineEvidence Equatable

Status: public API for kit/verb equality; not a vanilla SET GO.

- `WorldlineBehavior.CREEPER_FUSE` is the public identity. Token `creeper-fuse` maps to `atlas.scenario.creeper-fuse`.
- Development smoke ids (`m448-creeper-fuse-set`) import only as aliases; they are not the public identity.
- `WorldlineEvidence` equality is behavior token plus semantic SHA-256. Lane `vanilla`/`mod` is provenance only.
- `WorldlineEvidenceDiff` renders behavior/signature/signal mismatch. Ad-hoc field asserts stay out.
- `Creeper.stayUntilExplode` is the first public verb; the creeper-fuse smoke emits `WorldlineEvidence`.

## Unreleased - HTML Evidence Pages

Status: GO.

- Added `worldline trace html <left> [right] <output.html>`: deterministic
  self-contained pages for single-trace viewing and two-trace structural
  diffing with first-divergence highlighting and explicit verdicts.
- Added the neutral `analysis.TraceHtml` renderer: pure function of its
  inputs, no scripts or assets, escaped output, byte-identical re-renders so
  page digests are freezable evidence.
- Evidence: viewer and diff pages rendered from real controlled runs in the
  m19 smoke; frozen SHA-256 in `smokes/m19-html/smoke.properties`.

## Unreleased - Runtime Semantic Coverage

Status: GO.

- Added the neutral `coverage` module and `worldline coverage <scenario>
  [trace] [min-percent]`: dynamic mapping of DSL steps onto the closed
  semantic catalog's control categories plus role extraction from executed
  trace fields through the closed `SemanticFields` alias table.
- Added checksum-protected `WORLDLINE-COVERAGE/1` artifacts (`.wlcover`) and
  an optional floor-percentage gate (exit 3) for scenario-corpus richness
  checks; fully neutral, no runtime required.
- Evidence: all five mappable verbs classified (20% of 25 categories),
  executed-trace role extraction, artifact creation, and both threshold
  outcomes; frozen SHA-256 in `smokes/m18-coverage/smoke.properties`.

## Unreleased - Tick Profiling Budgets

Status: GO.

- Added `worldline profile <scenario> <seed> [budget.properties]`: per-
  controlled-tick wall-clock sampling with mod-callback attribution via the
  hook layer, deterministic nearest-rank aggregates, and canonical
  checksum-protected `WORLDLINE-PROFILE/1` reports that pair timings with the
  behavioral trace digest.
- Added the strict optional-key budget gate (exit 3 with per-key violations)
  for machine-relative regression control; timing values are never frozen
  evidence, only structure and gate outcomes are.
- Added the neutral `profiling` module (`ProfiledRunner`, `TickProfile`,
  `TickProfiledRun`, `ProfileBudget`, `ProfileReport`) with full unit
  coverage; adapter binding via `worldline.profile.provider`.
- Evidence: structural smoke with tight-budget rejection and generous-budget
  pass; frozen SHA-256 in `smokes/m17-profile/smoke.properties`.

## Unreleased - Time-Travel Debug REPL

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

## Unreleased - Differential Fuzzer

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

## Unreleased - Census, Seed Atlas, and Screen Export

Status: GO.

- Added `worldline census <out-dir>`: canonical `WORLDLINE-CENSUS/1` dumps of
  every registered block and item, all crafting recipes (mapped-field
  reflection), and all furnace smelts; byte-deterministic per section.
- Added `worldline atlas <seed> <radius-1..4> <out.html>`: boots the official
  dedicated server under any seed, walks the player in <=4-block glides
  across every chunk, decodes streamed chunks over the proven wire path, and
  renders a deterministic colored terrain page with a block legend.
- Added `worldline ui <out.html>`: exports the open inventory semantic tree
  (role/name/index/item/count) as a self-contained page.
- Evidence: census-cycle, seed-atlas, and ui-export smokes with frozen page/
  section digests.

## Unreleased - Mod API v2

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

## Unreleased - Attested Mod Test Run

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

## Unreleased - Multi-Mod Dependency Graph

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

## Unreleased - Public Scenario DSL

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

## Unreleased - Worldline Atlas port

- Ported the generated, fail-closed Atlas knowledge layer from the M88
  prototype onto the M469/TestKit mainline.
- Added Atlas status, lookup, search, gaps, coverage, evidence, graph, export,
  and milestone-delta commands without making Atlas a runtime dependency.
- Indexed semantic roles, controlled boundaries, invariants, milestone
  evidence, trace fields, ecosystem knowledge, and explicit uncertainty.
- Replaced milestone-number inference with explicit subsystem metadata across
  every smoke and froze legacy expected signatures in their MAP evidence.
- Added provenance-aware loader, API, mapping-set, and namespace records for
  the Babric/Ornithe ecosystem without vendoring third-party mappings.

## Unreleased - Worldline TestKit 0.2.1 experimental

- Changed the public plugin namespace to the GitHub-verifiable
  `io.github.lucasrgt.worldline.test` ID and
  `io.github.lucasrgt.worldline` Maven group, and explicitly declared that
  Configuration Cache is not yet supported.
- Added the binary Gradle plugin, an isolated
  `tests/worldline` Java 8 source set, Vitest-like run/list/watch/inspect/
  snapshot/minimize tasks, JUnit XML, conservative product discovery, and a
  Gradle TestKit functional suite.
- Added `worldline init`, `doctor`, and non-destructive `migrate`; pinned the
  Gradle wrapper; created ignored official-JAR drop zones; and added strict
  local, environment, global-profile, and shared-artifact resolution.
- Added frozen client/server identity checks, Git-tracking rejection, explicit
  hash-verified acquisition, offline TestKit distributions, Plugin Portal and
  GitHub release automation, CI integration, templates, and adoption docs.
- Migrated the BetaEnergistics, Butter, and AeroModelLib consumer suites to
  ordinary `*WorldlineTest.java` files under their `tests/worldline` builds.

- Added the Java 8 `worldline.test` authoring API with `test`/`it`,
  `describe`/`suite`, hooks, table cases, explicit modifiers, typed
  expectations, change assertions, snapshots, named steps, and promoted
  semantic selectors.
- Added the physical Java 8 `testmodel` boundary, per-test
  `worldline().runtime().seed().mod().run()` configuration, structured semantic
  divergence, mapping access/stability metadata, and seed-bearing results.
- Added the Java 21 isolated runner with fresh runtime sessions, visible
  flakiness, fail-closed `.only`, filtering, explicit shuffle, timeout
  inventories, scenario minimization, canonical failure artifacts, and an
  exclusive cross-process official-runtime lock.
- Added default, verbose, dot, JSON, JUnit, and agent reporters plus multi-spec
  discovery, bounded external classpaths, distinct file/test filters,
  `run`/`list`/`inspect`/`watch`/`minimize`, and strict project configuration.
- Added deterministic ignored API/runner packaging, the b1.7.3 provider, a
  ten-spec/30-test acceptance suite, and external Butter/AeroModelLib consumers.
- Licensed Worldline and TestKit under the MIT License for external use and
  redistribution.
- Documented the 0.x extension model, including external project layout,
  provider ownership, optimization evidence, legacy-loader limits, and the
  recommended Beta Energistics integration sequence.
- This is not a promoted stable release or a change to the official M469
  behavioral evidence. TestKit remains 0.x until external-mod use validates
  the authoring surface.

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
