# Changelog

All notable Worldline changes are recorded here. The project follows semantic
versioning for declared stable contracts; experimental adapter APIs may change
before they are promoted.

## 1.462.0 - Consolidated vanilla qualification

Status: GO candidate after central audit correction; published history is preserved.

- Retracted M549 because its hidden south-block fallback changed the cause
  while emitting the same signature.
- Retracted M552 because its TNT was ordinarily powered by an adjacent solid;
  the fixture did not isolate quasi-connectivity.
- Retracted M557 because the repeater observation was not tick-resolved and
  therefore did not prove a one-tick pulse.
- Retracted M564 because the lit and dark spawn fixtures changed both cover
  and substrate, so the negative was not causal evidence for light.
- Narrowed M555 to the behavior directly observed by its fixture: 24 rapid
  lever activations, an unpowered `75:4` state, a 400-tick recovery wait, and
  return to `76:4`. It no longer claims an observed eight-toggle threshold or
  100-tick window.
- Removed the four rejected smokes from the current smoke catalog. Their
  original v1.452.0, v1.454.0, v1.457.0, and v1.458.0 entries and tags remain
  below as an immutable record of what was published.
- Rejected unpublished M562 after central runtime qualification first stalled
  in second-frame placement and then produced an obstructed frame that could
  not light. Rejected unpublished M568 after two clean serialized runs both
  failed to observe the reloaded dropped-item Packet21.
- Qualified corrected-scope M555 plus M556, M563, and M569 in direct serialized
  official-server cycles. The final release gate also restores explicit
  signature coverage for the qualified M500-M525 DeepSeek batch while keeping
  M520 rejected.
- Restored M15's original two-thirds broad geometry-agreement invariant after
  repeated GUI qualification exposed scheduling variance at the later
  three-quarters threshold; the 100-position floor and required nonzero
  temporal divergence remain fail-closed.
- Replaced M64's probabilistic `/give` drop pickup with an official-format
  player-NBT fixture that fixes planks at personal slot 36 and the consumed
  workbench at slot 37, preserving the frozen workbench actions and signature.
- Replaced M56's probabilistic `/give` drop pickup with an official-format
  player-NBT fixture that fixes stone at personal slot 36 and places both peers
  within tracking range while preserving the rejected action, authoritative
  resync, accepted recovery, and signature.

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

## 1.461.0 - M567 Bed spawn set

Status: GO for official bed occupy plus cactus-death Packet9 at the bed together.

- Night occupy `26:8 -> 26:12` Packet17, morning leave standing, cactus Packet8 `0`, Packet9 respawn at the bed not `level.dat` world spawn.
- Distinct from M330 occupy/wake, M135 world-spawn respawn, and M469 void death without a bed.
- Repeated the complete family in two official server JVMs.

The frozen M567 semantic SHA-256 is
`aaad061b562df911b0b4c29784fe2beb4b0d5f1183dae8e29603cd3c2a838aed`.

## 1.460.0 - M555 Torch burnout set

Status: GO for official redstone-torch 76 burnout to 75 then recover together.

- Rapid lever toggles burnt north-face `76:4` to `75:4` inside the 100-tick window; a later neighbor update recovered `76:4`.
- Distinct from M312 single invert while powered and M182 floor `76:5`.
- Repeated the complete family in two official server JVMs.

The frozen M555 semantic SHA-256 is
`51a58a2129fecaba1f082e28aaa285177901ece62fb08c5e70d85fbcd3535713`.

## 1.459.0 - M566 Grass spread set

Status: GO for official grass-2 random-tick Packet53 onto lit dirt-3 together.

- Lit dirt samples adjacent to an 8-cell grass ring became `2`; stone-covered dirt stayed `3`. Exact tick and which sample converts are not hashed.
- Distinct from M238 grass place and M223 dirt place.
- Repeated the complete family in two official server JVMs.

The frozen M566 semantic SHA-256 is
`b80a81abefd273cf68a6495d0a825f6556e85176324d77bc1702832ffc448174`.

## 1.458.0 - M557 One-tick pulse set

Status: GO for official 1-tick repeater piston pulse that leaves a pushed block together.

- Floor lever through west 1-tick repeater `93:3` pulsed piston `33`; stone remained at `2:65:4` after retract.
- Distinct from M367 full lever hold and M144 sticky pull.
- Repeated the complete family in two official server JVMs.

The frozen M557 semantic SHA-256 is
`cd7816b4b28602a9d7bb4cb6e65bbfc8918216b84e075b8912af314905ec7c05`.

## 1.457.0 - M564 Spawn light set

Status: GO for official hostile Packet24 at light <=7 plus torch-light rejection together.

- Night `14000` creeper/zombie spawners emitted nearby type `50` or `54`; 49 floor torches `50:5` (light 14) kept those types absent.
- Distinct from M435 identity-only, M390 remaining-spawner identity, and M141 pig spawner.
- Repeated the complete family in two official server JVMs.

The frozen M564 semantic SHA-256 is
`45458f0fd9d3a18ec9205472afef562dea56312353613004d3fe5c1b8374d503`.

## 1.456.0 - M560 Portal scale set

Status: GO for official Overworld-to-Nether 8:1 portal coordinate scale together.

- Far Overworld portal `325,66,331` Packet9 `0->-1` landed within 128 of `floor(x/8),floor(z/8)=(40,41)` and farther than 128 from the source.
- Distinct from M132 activation, M133 near-spawn traversal, M134 roundtrip, and M382 frame-ignite.
- Repeated the complete family in two official server JVMs.

The frozen M560 semantic SHA-256 is
`d7eb052e1bc5fe6a71f3850bd4fb75b9470be6a2767c6617fb41f7138c54c50b`.

## 1.455.0 - M559 Double extender set

Status: GO for official sequenced sticky-29 then piston-33 two-cell payload travel together.

- Rear sticky `29` then front `33` moved cobble two cells west (`2,65,4 -> 0,65,4`).
- Distinct from M145 two-block payload on one piston and M147 12-block limit.
- Repeated the complete family in two official server JVMs.

The frozen M559 semantic SHA-256 is
`49d44fb82433fdcf9dcf8ca5201aa946b783e9d3539c9689d6a2284af36fac0f`.

## 1.454.0 - M549 Sticky BUD set

Status: GO for official sticky-29 primed QC plus neighbor-update extend together.

- Diagonal-above lever primed sticky `29:4` without extending; a north stone update BUD-extended `29:4 -> 29:12` with head `34:12`, then unpower pulled.
- Distinct from M547 immediate sticky QC and M548 regular-33 BUD.
- Repeated the complete family in two official server JVMs.

The frozen M549 semantic SHA-256 is
`d9de32a7e37b272dd97be1d211464f0bf67b7b66ba71ddedbf3742d0f345747b`.

## 1.453.0 - M553 Piston immovable set

Status: GO for official piston-33 rejection of chest, furnace, and spawner together.

- West pistons stayed `33:4`; chest `54`, furnace `61`, and spawner `52` did not move; destinations stayed air.
- Distinct from M146 obsidian-only and M147 12-block push limit.
- Repeated the complete family in two official server JVMs.

The frozen M553 semantic SHA-256 is
`6b35bf7c4b6f658370491bc20505538a93425b8309bc17d26f9d8b3d19ff06cf`.

## 1.452.0 - M552 TNT QC set

Status: GO for official TNT-46 quasi-connectivity prime plus Packet60 crater together.

- Lever on the stone ABOVE TNT primed Packet23 type `50` then Packet60 strength `4`; not flint-and-steel (M381) and not adjacent dust on the TNT cell.
- Distinct from M219 unprimed place and M137 detonate-without-prime.
- Repeated the complete family in two official server JVMs.

The frozen M552 semantic SHA-256 is
`a0ad8d6262175c29d1c7d1dadfcaf90f6a45d1db92c4c7dbbb63983a969b0732`.

## 1.451.0 - M550 Dispenser QC set

Status: GO for official dispenser-23 quasi-connectivity cobble eject together.

- Floor lever on the stone ABOVE dispenser `23:4` ejected Packet21 cobble `4x1` with no adjacent side lever on the dispenser.
- Distinct from M153/M333 adjacent-power eject. Arrows, buckets, and TNT stay unclaimed.
- Repeated the complete family in two official server JVMs.

The frozen M550 semantic SHA-256 is
`fbebc71b9da63b30d1c347778fb92cafea108114e52b79af79ae955487ef73db`.

## 1.450.0 - M548 Piston BUD set

Status: GO for official piston-33 neighbor-update pulse with no continuous power together.

- Torch place on the payload was the BUD update; moving piston `36:4` then self-cleared to `33:4` with the stone left behind (`power=none`).
- Distinct from M367 lever-power and M546 QC-held `33:12`.
- Repeated the complete family in two official server JVMs.

The frozen M548 semantic SHA-256 is
`64edc418a23140583ce5015dead697010582f99862cc5e19d6e8e7e53f02bcff`.

## 1.449.0 - M547 Sticky piston QC set

Status: GO for official sticky-29 quasi-connectivity extend plus pull together.

- Lever on the stone ABOVE west sticky `29` QC-extended `29:4 -> 29:12` with head `34:12`, then pulled the payload.
- Distinct from M546 regular-33 QC, M367 dual-arm motion, and M144 support-lever pull.
- Repeated the complete family in two official server JVMs.

The frozen M547 semantic SHA-256 is
`21af5dafa50bb529a1c0264a2be27d9b92aa0728c007fae07ecbef1547d92b1d`.

## 1.448.0 - M546 Piston QC set

Status: GO for official piston-33 quasi-connectivity extend plus retract together.

- Lever on the stone ABOVE west piston `33` QC-extended `33:4 -> 33:12` with head `34:4`; unpower retracted. Piston cell had no direct power.
- Distinct from M367 lever-on-piston, M142-M147 1:1 arms, and M427 place-facings.
- Repeated the complete family in two official server JVMs.

The frozen M546 semantic SHA-256 is
`10f2cb5507e55026ceafd72d6ab2c74a3ab228aad1924965b9844d149117d989`.

## 1.447.0 - M554 Extended head break set

Status: GO for official extended piston-33 base Packet14 leftover together.

- West piston `33:4` extended to `33:12` with head `34:4`; Packet14 of pick `257` on the base left both cells air and Packet21 `33`.
- Distinct from M367 retract-by-unpower, which keeps `33:4`. Head-first break and sticky leftover stay unclaimed.
- Repeated the complete family in two official server JVMs.

The frozen M554 semantic SHA-256 is
`2cc464442cf4d3f0a5f88c7cb81921c7594834d6c9114630b54798241b4c5cbf`.

## 1.446.0 - M469 Void death set

Status: GO for official void walk-off Packet8 death plus Packet9 respawn together.

- Packet13 walked below the kill plane with cap 9; Packet8 health `0`; Packet9 restored health `20`.
- Distinct from M135 wait-already-under-kill, M461 fall, and M465 env death.
- Repeated the complete family in two official server JVMs.

The frozen M469 semantic SHA-256 is
`52332cdbcd2108c4f8baa59811bffe40d9ba676283c851371bb2bee321f7ef98`.

## 1.445.0 - M467 Difficulty damage set

Status: GO for official Easy then Hard zombie melee Packet8 both `20->18` together.

- Type `54` melee on `difficulty=1` and `difficulty=3` both dropped health `20` to `18`.
- Official dedicated-server Easy branch; Hard `*3/2` is not reached. Distinct from armor and peaceful.
- Repeated the complete family in two official server JVMs.

The frozen M467 semantic SHA-256 is
`61e1ac15b1e84c70af6ec58f615e81db3d5a6ae0c3deaac931da803a16f459d7`.

## 1.444.0 - M462 Bow mob hit set

Status: GO for official player bow Packet23 type 60 hits on pig and zombie together.

- Bow `261` air-use hit Packet24 types `90` and `54` with Packet38 status `2`.
- Distinct from M436 collect, M332 shoot-only, and skeleton arrows.
- Repeated the complete family in two official server JVMs.

The frozen M462 semantic SHA-256 is
`bbe6e87049578c8e26c8cca6f79ed7ac1f3c530df498b2d9da63a8f195578e22`.

## 1.443.0 - M460 Monster bed interrupt set

Status: GO for official bed occupy plus hostile Packet24 interrupt together.

- Packet15 occupied head `26:8` to `26:12` with Packet17; type `54` returned it to `26:8`.
- Packet70 stayed `-1`; morning skip was rejected. Distinct from M330 occupy/wake.
- Repeated the complete family in two official server JVMs.

The frozen M460 semantic SHA-256 is
`252160a06c2d628ac1441c16105e90c2c1e0047f10a300061765a01948d87c61`.

## 1.442.0 - M459 Ghast fireball hit set

Status: GO for official Nether ghast Packet23 type 63 fireball hit together.

- Packet24 type `56` threw Packet23 type `63`; Packet60 strength `1` hit as Packet8 and/or crater.
- Distinct from M410 spawn-only type `63` and M411 pigman pork.
- Repeated the complete family in two official server JVMs.

The frozen M459 semantic SHA-256 is
`491a34451873fea634086ff4a8c83a68e25ff5a8ed43d75033d4ed22b63f5042`.

## 1.441.0 - M458 Slime touch set

Status: GO for official slime Packet24 size family plus AABB Packet8 contact together.

- Packet24 type `55` showed size-1 and larger metadata; walking in emitted Packet38 then Packet8.
- Distinct from M412 parent-split children and M423 slimeball.
- Repeated the complete family in two official server JVMs.

The frozen M458 semantic SHA-256 is
`84cc0baf6465c46adf5437018728a84b237f8d611fa461bcc6335932432f2d26`.

## 1.440.0 - M457 Spider leap set

Status: GO for official spider Packet24 leap toward the actor plus touch Packet8 together.

- Packet24 type `52` leaped toward the actor on the grass pad, then Packet8 hurt.
- Distinct from spider climb, string/cobweb, and natural-spawn identity.
- Repeated the complete family in two official server JVMs.

The frozen M457 semantic SHA-256 is
`c1acc30fb89383a980963eda9ae54bd6fcc4a2c8eaff785ee3a10b3206e3153c`.

## 1.439.0 - M452 Knockback cooldown set

Status: GO for official zombie melee Packet8 knockback plus hurt-time cooldown together.

- Packet24 type `54` melee dropped health `20` to `18` with a Packet13 pose away from the mob.
- A second contact inside vanilla hurt-time did not emit another Packet8 drop.
- Repeated the complete family in two official server JVMs.

The frozen M452 semantic SHA-256 is
`242841cb9a28e2404bdfba3f9787b624b4d14c6fdeb9e08665bc5522d4b5f441`.

## 1.438.0 - M448 Creeper fuse set

Status: GO for official creeper Packet40 proximity fuse before Packet60 together.

- Packet24 type `50` ignited via Packet40 state `1` while the actor stayed; Packet60 followed.
- Distinct from M391 crater hashing, M421 gunpowder, and M456 leave-cancel.
- Repeated the complete family in two official server JVMs.

The frozen M448 semantic SHA-256 is
`702d4dc074d1db9a965d74f49f1318cb05a4397c343a59b8fde15a3ab8f15505`.

## 1.437.0 - M455 Melee pursuit set

Status: GO for official zombie and skeleton Packet24 pursuit toward the actor pose together.

- Packet24 types `54` and `51` each emitted a Packet31/33/34 step toward the actor pose.
- Distinct from identity-only hostiles, death drops, skeleton archery, and zombie door break.
- Repeated the complete family in two official server JVMs.

The frozen M455 semantic SHA-256 is
`36fea72b3152e1d8b6245cfd8731ba14fa83aa5818bef04bab2ab838441de935`.

## 1.436.0 - M454 Peaceful despawn set

Status: GO for official Peaceful absence versus Easy persist of Packet24 hostiles together.

- Easy `difficulty=1` persisted creeper type `50` and zombie type `54`.
- Peaceful `difficulty=0` kept types `50/51/54` absent.
- Repeated the complete family in two official server JVMs.

The frozen M454 semantic SHA-256 is
`8a4c4acadf23008e8fed2fdbc1d9c05c903c65c527c3489dabee48e7d2183abe`.

## 1.435.0 - M453 Player death drops set

Status: GO for official void death plus three seeded hotbar Packet21 drops together.

- Packet8 health `20` to `0` from vanilla void; Packet21 stone `1`, cobble `4`, and dirt `3`.
- Distinct from M50 drop-current and from mob-drop families.
- Repeated the complete family in two official server JVMs.

The frozen M453 semantic SHA-256 is
`6d7e55c8c86f1540d7306a507b0a07af3ef9cbe3b6f6c79cf2b87663beab7ed0`.

## 1.434.0 - M450 Pigman anger set

Status: GO for official Nether pigman Packet7 group-aggro together.

- Two Packet24 type `57` identities; Packet7 sword `276` hurt one; neighbor aggroed.
- Distinct from M411 cooked-pork drop and M437 lightning identity pair.
- Repeated the complete family in two official server JVMs.

The frozen M450 semantic SHA-256 is
`ae24558c960284894ed1577e583f5fbbdcfd65ebfd4ed48af6687179d2ccf098`.

## 1.433.0 - M445 Skeleton ranged AI set

Status: GO for official skeleton Packet23 type 60 archery with skeleton thrower together.

- Packet24 type `51` fired two Packet23 type `60` arrows whose thrower was that skeleton.
- Distinct from player bow `261` and from skeleton bone `352`.
- Repeated the complete family in two official server JVMs.

The frozen M445 semantic SHA-256 is
`59d850eaeeb297f3879633c70a546d1aa4da2de0618852cb9f3e802a8ec6533b`.

## 1.432.0 - M436 Remaining arrow life set

Status: GO for official remaining bow-arrow land plus dropped-arrow collect together.

- Bow `261` air-use landed Packet23 type `60`; Packet14 dropped arrow `262` as Packet21.
- Packet103 restored arrow `262` after the official pickup delay.
- Repeated the complete family in two official server JVMs.

The frozen M436 semantic SHA-256 is
`9a370fd980f9abd2ed3f852ff575a9dae9c9b0f461c73fa548d131b40077011c`.

## 1.431.0 - M422 Skeleton bone set

Status: GO for official skeleton bone drop plus bone-meal wheat together.

- Packet24 type `51` was killed with diamond sword `276`; Packet21 bone `352` was observed.
- Bone `352` milled to `351x3:15` and Packet15 matured wheat `59:0` to `59:7` before midnight.
- Repeated the complete family in two official server JVMs.

The frozen M422 semantic SHA-256 is
`c68f7f0903a8483cfac08ca4d91735085c70835d9f5e485f8055423a3ebc6dc4`.

## 1.430.0 - M444 Remaining mob drops rest

Status: GO for official remaining pig pork plus sheep wool death drops together.

- Packet24 type `90` dropped pork `319`; type `91` dropped undyed wool `35:0`.
- Both remaining drops share one frozen SET distinct from M388/M389/M409/M411.
- Repeated the complete family in two official server JVMs.

The frozen M444 semantic SHA-256 is
`4f0cf6fc97f045251947014072b407aae095b6419fb3c3ab94c50722f7db8f66`.

## 1.429.0 - M443 Remaining bucket rest set

Status: GO for official remaining empty-bucket still-source versus flowing pickup together.

- Empty bucket `325` scooped still water `9:0` and still lava `11:0`; flowing `9:1` and `11:2` were rejected.
- Filled buckets and empty source cells persisted after a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M443 semantic SHA-256 is
`b556b71fd57896aa06fbb39f5088d8f96e6c8a64076014c7d7391b961c669eb7`.

## 1.428.0 - M442 Remaining record place set

Status: GO for official remaining two-jukebox disc insert together.

- Two jukeboxes `84` accepted gold disc `2256` and green disc `2257` and became `84:1`.
- Both cells persisted after a clean save plus fresh login; eject is unclaimed.
- Repeated the complete family in two official server JVMs.

The frozen M442 semantic SHA-256 is
`b70badf841ffc29e7c9adb0c7d29b5c2b687a43a5bcdb0e85e065170d1f7551a`.

## 1.427.0 - M440 Remaining dye rest set

Status: GO for official remaining personal 2x2 dye mixes together.

- Ink plus bone meal yielded light gray `351x3:7`; gray plus bone meal yielded `351x2:7`; purple plus pink yielded magenta `351x2:13`.
- Those stacks persisted after a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M440 semantic SHA-256 is
`0334f546ce0368581cb95d0fcb41d97e63d257acb76e91c53b41c849cfac594d`.

## 1.426.0 - M439 Remaining ore place set

Status: GO for official remaining coal, lapis, and unlit redstone ore place together.

- Packet15 placed coal ore `16:0`, lapis ore `21:0`, and unlit redstone ore `73:0`.
- All three cells persisted after a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M439 semantic SHA-256 is
`0c58ca403f7064fde875a5257d07193fe9916277c21455b47ac366ab28b828ab`.

## 1.425.0 - M438 Remaining clock map set

Status: GO for official remaining clock craft plus empty-map air-use together.

- Workbench `58` crafted clock `347`; Packet15 air-use of empty map `358` stayed `358:1:0`.
- Held clock and unfilled map persisted after a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M438 semantic SHA-256 is
`9ebe2cca746ab29d741407b8788d0b10a7e942cd691b868eb0d1d2f00e83eb58`.

## 1.424.0 - M437 Lightning pig set

Status: GO for official Overworld pig plus Nether pigman identity pair together.

- Overworld Packet24 type `90` and Nether Packet24 type `57` were observed from spawners.
- Lightning Packet71 is unclaimed; Packet23 tracker is absent.
- Repeated the complete family in two official server JVMs.

The frozen M437 semantic SHA-256 is
`536016d5292cf2d747ea4a029011726719795579c19e8507ec912154e9bd77db`.

## 1.423.0 - M435 Remaining natural spawns

Status: GO for official remaining natural Packet24 hostile identities together.

- Midnight with `spawn-monsters=true` observed at least two of types `50`, `51`, `52`, and `54`.
- No mob spawner was placed and MobSpawner `EntityId` was not rewritten.
- Repeated the complete family in two official server JVMs.

The frozen M435 semantic SHA-256 is
`a81852d5c2fb5cff300186da2b5d585a72f06f637dffb1942c4a8f1f2284d6d3`.

## 1.422.0 - M432 Remaining rail geometry set

Status: GO for official remaining rail slope and curve metadata together.

- Rail `66` placed remaining slope `66:2` plus south-east curve `66:6`.
- Both cells persisted after a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M432 semantic SHA-256 is
`3da03f5b4d6dd509fa5fc0925d5ea7422d5cd6ddb96e7acb84b5854de2ab61b1`.

## 1.421.0 - M431 Remaining bed orient set

Status: GO for official remaining bed west/north/east halves together.

- Bed `355` placed remaining halves `26:1/9`, `26:2/10`, and `26:3/11`.
- All six cells persisted after a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M431 semantic SHA-256 is
`8aa709e05da8be4a281e9eded3c6297e0f4236a515d73d60178570c69cf303a1`.

## 1.420.0 - M430 Remaining painting motives

Status: GO for official remaining Packet25 motive-size walls together.

- Item `321` on 4x2, 4x3, and 4x4 west-face walls spawned three Packet25 arts.
- Two headless peers observed the same entity identities; titles are not hashed.
- Repeated the complete family in two official server JVMs.

The frozen M430 semantic SHA-256 is
`1504c14913948dca32f92c0dacff830c42a51f7c402354b7a872fc92af410e09`.

## 1.419.0 - M429 Remaining attach faces

Status: GO for official remaining ladder, trapdoor, and wall-sign west/south/north faces together.

- Ladder `65:4/3/2`, closed trapdoor `96:2/1/0`, and wall sign `68:4/3/2` were placed.
- All nine cells persisted after a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M429 semantic SHA-256 is
`d00079b30c3f58f9f2a197e5a0a27c88880e15c28c3eaf88806d4502ebc2eb2b`.

## 1.418.0 - M428 Remaining door orient set

Status: GO for official remaining wooden-door hinge/face pairs together.

- Wooden door `324` placed remaining faces `64:0/8`, `64:1/9`, `64:2/10`, and `64:3/11`.
- All eight cells persisted after a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M428 semantic SHA-256 is
`10dad6f6b34f4140a80e7a09abeebaa5ff502bc6eee4607964a64dae72626bd2`.

## 1.417.0 - M427 Remaining piston orient set

Status: GO for official remaining piston and sticky-piston place-facings together.

- Piston `33` and sticky piston `29` placed remaining metas `0/2/3/4/5`.
- All ten cells persisted after a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M427 semantic SHA-256 is
`467d62056ad74b5561c6e6bf67533b1608d7fc66644062154b00b81109e8ad76`.

## 1.416.0 - M426 Remaining redstone faces

Status: GO for official remaining lever wall/ground faces plus remaining repeater delay-facing family together.

- Lever `69` placed remaining wall faces `69:2/3/4` plus two ground attachments.
- Repeater `356` placed remaining facings `93:0/1/2`; east delay-2 powered to `94:5`.
- Repeated the complete family in two official server JVMs.

The frozen M426 semantic SHA-256 is
`1bb55855bc7d7a3c3f9eef22fd7e235e02c3e5220a782fb29ed29a27bb69b44e`.

## 1.415.0 - M425 Remaining machine faces

Status: GO for official remaining dispenser, furnace, and pumpkin look-yaw facings together.

- Dispenser `23:2/5/4`, furnace `61:5/3/4`, and pumpkin `86:2/3/0` were placed from remaining look yaws.
- All nine cells persisted after a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M425 semantic SHA-256 is
`5f5f8026b3aef5768a963db53d9393ac9ed86b766118d805407d5cf5b11a5dbf`.

## 1.414.0 - M424 Furnace cart motion set

Status: GO for official furnace-cart type 12 coal push plus detector occupancy together.

- Unfueled type `12` held idle on rail `66`; coal `263` Packet7 consumed and pushed the cart.
- Detector `28:0` became `28:8` after the fueled move.
- Repeated the complete family in two official server JVMs.

The frozen M424 semantic SHA-256 is
`536398b8e8c64ca3dc8e527842ae556bf4175363fc0b8e554d2ba0ec52811b1b`.

## 1.413.0 - M423 Slimeball set

Status: GO for official size-1 slimeball drop plus sticky-piston craft together.

- Packet24 type `55` size-1 death emitted Packet21 slimeball `341`.
- Workbench `58` crafted sticky piston `29` from piston `33` plus `341`.
- Repeated the complete family in two official server JVMs.

The frozen M423 semantic SHA-256 is
`8a525200f72521e3f129de58b27232e197c39cf2d41da689d2772e9a830ac411`.

## 1.412.0 - M421 Creeper gunpowder set

Status: GO for official creeper gunpowder drop plus TNT craft together.

- Packet24 type `50` was killed with diamond sword `276`; Packet21 gunpowder `289` was observed.
- Workbench `58` crafted TNT `46` from `289` plus sand `12`; TNT stayed in inventory.
- Repeated the complete family in two official server JVMs.

The frozen M421 semantic SHA-256 is
`f01c7a65ddde0ddb0cd8f27f6e1c76e896f866c0bf9cc6f8af973bd1def648dc`.

## 1.411.0 - M420 Wolf tame set

Status: GO for official wolf Packet24 type 95 bone-tame plus dye collar together.

- Spawner retargeted to Wolf; Packet24 type `95` was tamed with bone `352` to Packet38 status `7`.
- Packet7 dye `351:4` was used on the living tamed wolf; no Packet38 status 3 death.
- Repeated the complete family in two official server JVMs.

The frozen M420 semantic SHA-256 is
`8268a761729c8e58ce515e8c1abb5065fa4782f824f83d9f2e6072f6e46d1833`.

## 1.410.0 - M441 Remaining food rest set

Status: GO for official remaining cookie and mushroom-stew air-eats together.

- Cookie `357` healed 1; mushroom stew `282` healed 8 and left bowl `281`.
- Golden apple `322` stays in M374; this rest family is cookie plus stew.
- Repeated the complete family in two official server JVMs.

The frozen M441 semantic SHA-256 is
`a742d0481ec2e053071b64ffb13a565582bd3dbbc76859b4d650f2a8b74ac5b7`.

## 1.409.0 - M434 Remaining sponge glass ice

Status: GO for official sponge, glass, and ice place together.

- Packet15 placed sponge `19`, glass `20`, and ice `79` on a raised stone fixture.
- Ice stayed `79` with no torch melt; all three cells persisted.
- Repeated the complete family in two official server JVMs.

The frozen M434 semantic SHA-256 is
`0716150d188414cd60d0bebe7aa70f27ace8a376a47f6e0a912fc026e8ab63b5`.

## 1.408.0 - M433 Remaining chest orient set

Status: GO for official isolated look-yaw chests plus EW and NS adjacent pairs together.

- Packet15 placed two isolated chests `54` from look `-90` and `90`.
- East-west and north-south adjacent pairs were placed in the same session and persisted.
- Repeated the complete family in two official server JVMs.

The frozen M433 semantic SHA-256 is
`b9750e81a03028d1bb7345d6699d951772dea723fefb2cb303312f4c43423f03`.

## 1.407.0 - M413 Fire spread set

Status: GO for official netherrack fire spreading onto planks, leaves, and wool together.

- Flint-and-steel `259` placed fire `51` on netherrack `87`.
- Adjacent planks `5`, leaves `18`, and wool `35` caught or consumed while the netherrack flame persisted.
- Repeated the complete family in two official server JVMs.

The frozen M413 semantic SHA-256 is
`e8fdef86a6fe2bd49b4575a296bc67cfe62dce1f2eb89aefd7ca2e89aa70843c`.

## 1.406.0 - M419 Remaining netherrack place

Status: GO for official Nether netherrack, soul sand, and glowstone place together.

- Dimension `-1` login; Packet15 placed netherrack `87`, soul sand `88`, and glowstone `89`.
- All three cells survived a clean save plus fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M419 semantic SHA-256 is
`c7dec53dcc70e1baa573a851f8e296853cfe16d36ddd182d1cfd5e83a8a4dea7`.

## 1.405.0 - M418 Remaining obsidian place

Status: GO for official four-cell obsidian frame fragment plus pick harvest together.

- Packet15 placed four obsidian `49` cells as an unlit L-shaped frame fragment.
- Diamond pickaxe `278` harvested the cap cell; Packet21 obsidian `49` dropped; portal `90` stayed absent.
- Repeated the complete family in two official server JVMs.

The frozen M418 semantic SHA-256 is
`7c15aa18aedb3ac5e34f9b7fbc2836311b51f88fc0737ed40298e3d3e65be80e`.

## 1.404.0 - M417 Remaining TNT place

Status: GO for official two-cell TNT place plus flint-and-steel Packet60 chain together.

- Packet15 placed two TNT `46` cells two apart; flint-and-steel `259` primed the first.
- Packet60 strength `4` chained to the second TNT cell.
- Repeated the complete family in two official server JVMs.

The frozen M417 semantic SHA-256 is
`153e7f2258e4d355e0e2c070a630aebe6dfa4262d98a3e4aa3e99b8f99e0205d`.

## 1.403.0 - M416 Remaining bookshelf place

Status: GO for official bookshelf craft, two-cell place, and empty harvest together.

- Workbench `58` crafted block `47` from planks `5` plus books `340`.
- Two `47` cells were placed; gold axe `286` harvested one to air with no Packet21 book `340`.
- Repeated the complete family in two official server JVMs.

The frozen M416 semantic SHA-256 is
`63f78903270a88d3a7b5dafcb9aae55b9ffdacf7d785ff4b0f3d7616a975cc64`.

## 1.402.0 - M415 Water cobble set

Status: GO for official flowing-lava plus water cobble in two trenches together.

- Still lava `11` flowed to `11:2` / moving `10`; water `9` hardened each flowed cell to cobble `4`.
- Both lava sources stayed lava sources.
- Repeated the complete family in two official server JVMs.

The frozen M415 semantic SHA-256 is
`bf5ec9eaf7f4f9ec7cf8652c8bdef0af40a1d8fa89b618d519dc571fddc66148`.

## 1.401.0 - M414 Lava obsidian set

Status: GO for official still-lava plus water obsidian in two basins together.

- Lava bucket `327` placed still lava `11`; water bucket `326` placed still water `9` beside each source.
- Both lava-source cells hardened to obsidian `49`.
- Repeated the complete family in two official server JVMs.

The frozen M414 semantic SHA-256 is
`856101df96a1dea04d9f18e7a1ceef3018dce576227d046030271fa67825fbff`.

## 1.400.0 - M412 Slime split set

Status: GO for official slime Packet24 type 55 parent death plus child type-55 split together.

- Slime-chunk `-2,-2` cave below `y=16`; spawner retargeted to Slime with spawn-monsters.
- Packet24 type `55` parent was killed with diamond sword `276`; child type-`55` Packet24 appeared.
- Repeated the complete family in two official server JVMs.

The frozen M412 semantic SHA-256 is
`04232de5b9eb6e2e741dbbf008ade42638370d907b361856800b70fe8cb6e59b`.

## 1.399.0 - M411 Zombie pigman set

Status: GO for official Nether pigman Packet24 type 57 and cooked-pork 320 drop together.

- Nether login on netherrack; spawner retargeted to PigZombie with spawn-monsters.
- Packet24 type `57` was killed with diamond sword `276`; Packet21 cooked pork `320` was observed.
- Repeated the complete family in two official server JVMs.

The frozen M411 semantic SHA-256 is
`c448868efb22d1c2a50bab9554f6c30de3f184d9cc2eb129103068be9868ae84`.

## 1.398.0 - M410 Ghast fireball set

Status: GO for official Nether ghast Packet24 type 56 and fireball Packet23 type 63 together.

- Nether login on netherrack; spawner retargeted to Ghast with spawn-monsters.
- Packet24 type `56` threw Packet23 type `63` whose thrower was the ghast.
- Repeated the complete family in two official server JVMs.

The frozen M410 semantic SHA-256 is
`4a77f0136d56574b37e6aca69072e884a92ea9240a1904aca9aaaa8170e08b76`.

## 1.397.0 - M409 Spider string set

Status: GO for official spider string drop and cobweb place together.

- Spawner retargeted to Spider; Packet24 type `52` was killed with diamond sword `276`.
- Packet21 string `287` and cobweb `30:0` were observed in the same session.
- Repeated the complete family in two official server JVMs.

The frozen M409 semantic SHA-256 is
`625db8d839633b99daf0e73b098ad644d6f23e3d9ed4dda4f187687c1fe26fc4`.

## 1.396.0 - M408 Squid ink set

Status: GO for official squid water-habitat and ink-sac drop together.

- Packet24 type `94` spawned in seed water; Packet7 diamond sword `276` killed it.
- Packet21 ink sac `351:0` was observed.
- Repeated the complete family in two official server JVMs.

The frozen M408 semantic SHA-256 is
`4f3c68e6439036720158970ea6fb62f2db5d9bb980f42850dbb0cfdf53ac0f41`.

## 1.395.0 - M407 Chicken egg set

Status: GO for official chicken type 93 plus egg 344 / Packet23 type 62 together.

- Spawner retargeted to Chicken; Packet24 type `93` appeared in the same session as egg `344`.
- Bounded lay wait plus air-use Packet23 type `62` froze the egg family.
- Repeated the complete family in two official server JVMs.

The frozen M407 semantic SHA-256 is
`a27d5e84d4fc5e08292a9a78c2ebccf8027e9441118ed789ef3adc30d8ff97a6`.

## 1.394.0 - M406 Sheep dye set

Status: GO for official rose-red and dandelion-yellow living-sheep dyes together.

- Two type-`91` sheep were dyed with `351:1` and `351:11`, then sheared.
- Packet21 wool `35:14` and `35:4` dropped without Packet38 status 3.
- Repeated the complete family in two official server JVMs.

The frozen M406 semantic SHA-256 is
`0c2857eb2e2bf4aaa39c631eced8f47d470862396ae8e7981d41c6c0a0775cb7`.

## 1.393.0 - M405 Pig saddle set

Status: GO for official pig saddle consume and Packet39 mount together.

- Packet24 type `90` pig received saddle `329` via Packet7 button 0, then empty-hand mount.
- Packet39 attach bound passenger to that pig; the saddle stack was consumed.
- Repeated the complete family in two official server JVMs.

The frozen M405 semantic SHA-256 is
`a27d2ce0c705f4fe5af56c8e35b8ec7c212956eaff46a764ce610d54f40c06d9`.

## 1.392.0 - M404 Remaining cart break

Status: GO for official empty-cart and chest-cart Packet7 breaks together.

- Packet23 type `10` broke to Packet21 minecart `328`; type `11` broke to `328` plus chest `54`.
- Diamond sword `276` Packet7 button `1` attacked both objects.
- Repeated the complete family in two official server JVMs.

The frozen M404 semantic SHA-256 is
`8a80558c9383a317d0d6a8f145c940ff21cb07ffb3649aa4c564214adde79bcf`.

## 1.391.0 - M403 Remaining boat break

Status: GO for official two-boat Packet7 break wreckage together.

- Packet15 spawned two Packet23 type `1` boats; empty-hand Packet7 button `1` broke both.
- Packet21 plank `5` and stick `280` wreckage was shared by two peers.
- Repeated the complete family in two official server JVMs.

The frozen M403 semantic SHA-256 is
`34eb6766ee9194e30d2efd5712a5e932110351176e336e526d7c6f23a877dedc`.

## 1.390.0 - M402 Remaining detector rail

Status: GO for official detector rail unpowered then cart-occupied together.

- Packet15 placed detector `28:0`; minecart `328` spawned Packet23 type `10` and set occupancy `28:8`.
- Occupied detector persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M402 semantic SHA-256 is
`00ed23852b2822be0b8b8766debc5cf5049c7e54b7c106f0e7c8d6a5028b8ab3`.

## 1.389.0 - M401 Remaining redstone wire

Status: GO for official redstone-wire cross, line, and elbow shapes together.

- Packet15 placed unpowered dust `55:0` as a four-arm cross, an east-west line, and a south-east elbow.
- All three centers persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M401 semantic SHA-256 is
`b37e39c18b5b7ba396453c42ce9a726e1b0b51ab26949df34031ab9c9ddcd82e`.

## 1.388.0 - M400 Remaining torch faces

Status: GO for official wall-torch faces east, west, south, and north together.

- Packet15 placed torch `50:1`, `50:2`, `50:3`, and `50:4` on the four walls of one stone column.
- All four cells persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M400 semantic SHA-256 is
`ce7b2efbd3293b6dc413e9dd2c1b1c8af938af338cd70f50f3b973772d173868`.

## 1.387.0 - M399 Wooden button set

Status: GO for official stone-button wall faces east, west, south, and north together.

- Packet15 placed stone button `77:1`, `77:2`, `77:3`, and `77:4` on the four walls of one stone column.
- All four unpowered cells persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M399 semantic SHA-256 is
`898b58fa0f849df159f7bfcfde243b0957fddcd580770518251b1721cbf21c90`.

## 1.386.0 - M398 Jukebox eject set

Status: GO for official gold and cat disc insert, play, and Packet21 eject together.

- Packet15 inserted discs `2256` and `2257` into two jukeboxes; Packet61 `1005` played both.
- Gold axe Packet14 broke both playing cells and Packet21 ejected each stored record.
- Repeated the complete family in two official server JVMs.

The frozen M398 semantic SHA-256 is
`21d9a2123e3a3041573a22722d268dec75ee1d0d27d84fe0ae6f22e187f2bd8f`.

## 1.385.0 - M397 Dispenser projectiles

Status: GO for official dispenser snowball and egg Packet23 ejections together.

- Trap window loaded snowball `332` and egg `344`; a lever pulse ejected Packet23 types `61` and `62`.
- Reopened Trap slots 0 and 1 were empty.
- Repeated the complete family in two official server JVMs.

The frozen M397 semantic SHA-256 is
`66d497bee36abdc673c44336dad9a75afcc08fcf7ade36676c652023100b1731`.

## 1.384.0 - M396 Remaining wool crafts

Status: GO for official magenta, light-blue, and lime dyed-wool crafts together.

- Personal 2x2 mixed white wool `35:0` with magenta, light blue, and lime dyes.
- Results were wool `35:2`, `35:3`, and `35:5`.
- Repeated the complete family in two official server JVMs.

The frozen M396 semantic SHA-256 is
`7bd1423c0f7af5c289a638d55eb9b16ec8b709217f849b00e95b0a3316990c54`.

## 1.383.0 - M395 Remaining dye mix

Status: GO for official cyan, pink, and light-blue 2x2 dye mixes together.

- Personal 2x2 mixed cactus green+lapis, rose red+bone meal, and lapis+bone meal.
- Results were cyan `351x2:6`, pink `351x2:9`, and light blue `351x2:12`.
- Repeated the complete family in two official server JVMs.

The frozen M395 semantic SHA-256 is
`1ba82fec7effc4370c0a4169136f177851484511a3b86bf1d2aaf76134e1491c`.

## 1.382.0 - M394 Remaining slab place

Status: GO for official sandstone, wood, cobble, and double slab placement together.

- Packet15 placed sandstone slab `44:1`, wood slab `44:2`, cobble slab `44:3`, and double slab `43:0`.
- All four cells persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M394 semantic SHA-256 is
`7939076b43b10ef5972487f306388abb58dafba8b1ca28923a3fb952ef2c6a9f`.

## 1.381.0 - M393 Stair facing set

Status: GO for official oak and cobble stairs look-yaw facings together.

- Packet15 placed oak stairs `53:0`/`53:1` and cobble stairs `67:0`/`67:1` from look yaw `-90` and `90`.
- All four cells persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M393 semantic SHA-256 is
`1e94922033cceeec477b29842f80b9bce86737cb240b266bad8ad4cf93cf0253`.

## 1.380.0 - M392 Remaining fluid flow

Status: GO for official horizontal still-water and still-lava flow together.

- Dirt gates opened to air; still water `9:0` flowed as `9:1` and still lava `11:0` flowed as `11:2`.
- Sources stayed still and both flowed cells persisted after a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M392 semantic SHA-256 is
`8ec5aefbab73a3cd36a48185fa30c6266c70c3392ce80a5b319f8a6d94f2cfba`.

## 1.379.0 - M391 Creeper explode set

Status: GO for official creeper Packet60 strength 3 destroying dirt and wool together.

- Spawner retargeted to Creeper; Packet24 type `50` fused by proximity and exploded at Packet60 strength `3`.
- Dirt `3` and wool `35` crater cells were destroyed and persisted as air.
- Repeated the complete family in two official server JVMs.

The frozen M391 semantic SHA-256 is
`2a74b9f63925b31966343a26c78c5b6d87dcdb84096822099fe3988f5d59b771`.

## 1.378.0 - M390 Remaining spawner set

Status: GO for official creeper and spider Packet24 identities together.

- Spawners retargeted to Creeper and Spider; Packet24 types `50` and `52` were observed after midnight.
- Identities stayed distinct from M141 pig and M363 zombie/skeleton.
- Repeated the complete family in two official server JVMs.

The frozen M390 semantic SHA-256 is
`543ebd4ab455f716f9f706ba3647dbe861dfbe4f81d65c002df093f92e401215`.

## 1.377.0 - M389 Animal drops set

Status: GO for official cow leather and chicken feather drops together.

- Spawners retargeted to Cow and Chicken; Packet24 types `92` and `93` were killed with diamond sword `276`.
- Packet21 leather `334` and feather `288` were observed.
- Repeated the complete family in two official server JVMs.

The frozen M389 semantic SHA-256 is
`761e3177132b22cd98c5dd6a4fa802903098e923c7e0c31115dfae512c06213b`.

## 1.376.0 - M388 Hostile drops set

Status: GO for official zombie feather and skeleton arrow drops together.

- Spawners retargeted to Zombie and Skeleton; Packet24 types `54` and `51` were killed with diamond sword `276`.
- Packet21 feather `288` and arrow `262` were observed.
- Repeated the complete family in two official server JVMs.

The frozen M388 semantic SHA-256 is
`af71fe63de80f6405617a61a51d16c2027b51a4b9d198ef70cc5286faa026b45`.

## 1.375.0 - M387 Remaining light set

Status: GO for official glowstone, jack-o-lantern, and floor-torch light cells together.

- Packet15 placed glowstone `89:0`, jack-o-lantern `91:1`, and floor torch `50:5`.
- All three light cells persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M387 semantic SHA-256 is
`c8fb22dfee19b993ff3351bf0dfcb8de29c0975c84ee50c94848cd2d0e4c6d70`.

## 1.374.0 - M386 Ice snow melt set

Status: GO for official ice-to-water and snow-to-air light melt together.

- Floor torch `50:5` melted ice `79` to still water `9:0` and snow layer `78` to air.
- Both outcomes persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M386 semantic SHA-256 is
`00d10f8cca091d8efcf6f005b84e192d110161deafabfb6a71d69862a5de6b7a`.

## 1.373.0 - M385 Leaf decay set

Status: GO for official oak, spruce, and birch leaf decay together.

- Logs `17:0/1/2` were removed; leaves `18:8/9/10` decayed to air with Packet21 leaf items.
- The decayed cells persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M385 semantic SHA-256 is
`3974fe1e9ab8e39e20e8122dce05d183745ba923b5b1dd4306f63c308e0f2e1c`.

## 1.372.0 - M384 Cactus sugar set

Status: GO for official cactus and sugar-cane growth together.

- Cactus `81` on sand grew to height at least 2; sugar cane `83` beside water grew to height at least 2.
- Both plants persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M384 semantic SHA-256 is
`ebe81626228e8dc034975562ddc312713b9877d4020a97cec9b6e38884191824`.

## 1.371.0 - M383 Mushroom place set

Status: GO for official brown and red mushroom place together.

- Brown mushroom `39` on dirt `3` and red mushroom `40` on netherrack `87` under a roof.
- Both cells persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M383 semantic SHA-256 is
`3a737afbb664a8e1a32858a9e371ced9062f5a30950b631a9372829456fa9a21`.

## 1.370.0 - M382 Portal obsidian set

Status: GO for official 14-obsidian nether-portal frame plus interior `90` together.

- Fourteen obsidian `49` cells framed a 2x3 interior that flint-steel `259` filled with portal `90:0`.
- The frame and interior persisted in Overworld dimension `0`.
- Repeated the complete family in two official server JVMs.

The frozen M382 semantic SHA-256 is
`6892e4aa2cd98f329d9e6c1b83cf4feed463e1ad996fe3afe61a0a36f8778f56`.

## 1.369.0 - M381 Tnt prime set

Status: GO for official TNT flint-steel prime plus Packet60 crater together.

- Flint and steel `259` primed TNT `46`; Packet23 type `50` then Packet60 strength `4`.
- The crater persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M381 semantic SHA-256 is
`6cf1cfe074d14a3c856cf768c9a8b9cdc9cfa573b8ee2e901445db31692bfad5`.

## 1.368.0 - M380 Trapdoor family set

Status: GO for official trapdoor four-face toggle together.

- Trapdoor `96` on south/north/east/west faces toggled `1/0/3/2` to open then closed.
- The closed four-face set persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M380 semantic SHA-256 is
`ab78b72d72f7fa3016aff5ef1e7d1fa6d51961bb14c02d74afa5e1a5ecf036e7`.

## 1.367.0 - M379 Iron door set

Status: GO for official iron-door halves plus lever power together.

- Iron door `71` lower/upper went `0/8→4/12→0/8` from lever `69:1→9→1`.
- The closed door and lever persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M379 semantic SHA-256 is
`9d887adb7cbebcca0c805d02f84507310ea3211b6e1abb774ec7e7ae8d3e4f0c`.

## 1.366.0 - M378 Boat water set

Status: GO for official boat spawn plus Packet39 ride and detach together.

- Boat `333` spawned Packet23 type `1` on still water `9`.
- Empty-hand Packet7 mounted then detached with Packet39 attach/detach.
- Repeated the complete family in two official server JVMs.

The frozen M378 semantic SHA-256 is
`bdd585b5e79c816f4761039c63a02aa8e9f6164e77d7baa4fa4b3980a6a8d905`.

## 1.365.0 - M377 Powered rail motion

Status: GO for official powered-rail boost plus detector occupancy together.

- Powered rail `27` and detector rail `28` went `0→8` under minecart Packet23 type `10`.
- Unpowered hold stayed idle; powered motion was observed with redstone torch `76:5`.
- Repeated the complete family in two official server JVMs.

The frozen M377 semantic SHA-256 is
`c383cb26d4289064f7ced386bb9c7cfc9cdb68545275f438464e17ef5a161977`.

## 1.364.0 - M376 Remaining shovel breaks

Status: GO for official clay, snow, snow-block, and soul-sand shovel harvests together.

- Gold shovel `284` broke clay `82` to `337`, snow `78`/`80` to `332`, and soul sand `88` to `88`.
- Repeated the complete family in two official server JVMs.

The frozen M376 semantic SHA-256 is
`40c64c0c07f6bc2b0dd8ed47b2526c1b5ef81a70c4b44720126cc44bc5d15c52`.

## 1.363.0 - M375 Remaining pick breaks

Status: GO for official mossy cobble, gold ore, and obsidian pick harvests together.

- Gold pick `285` and diamond pick `278` broke mossy cobble `48`, gold ore `14`, and obsidian `49`.
- Packet21 dropped `48`, `14`, and `49`.
- Repeated the complete family in two official server JVMs.

The frozen M375 semantic SHA-256 is
`22503c04e191d5edd6c2374799f5062269ff1e38d71c15709e468a2d2e787869`.

## 1.362.0 - M374 Remaining food eat

Status: GO for official apple, cooked pork, and golden apple heals together.

- Apple `260` healed `16→20`, cooked pork `320` healed `12→20`, golden apple `322` healed `10→20`.
- Each eaten stack became empty and the final inventory persisted.
- Repeated the complete family in two official server JVMs.

The frozen M374 semantic SHA-256 is
`8039053be1dc2477fd129e75dd6f6facd47634f0d8dc9e0be131b9750c9e2215`.

## 1.361.0 - M373 Milk bucket set

Status: GO for official cow milk fill and drink together.

- Packet7 button 0 filled empty bucket `325` from cow Packet24 type `92` to milk `335`.
- Packet15 air-use drank milk back to empty `325` with health remaining `20`.
- Repeated the complete family in two official server JVMs.

The frozen M373 semantic SHA-256 is
`0def850e0165e277e1055538ab58e3a7772dcf0239f16acbc88f430b10e9a77c`.

## 1.360.0 - M372 Placeable item crafts

Status: GO for official painting, sign, and bowl crafts together.

- Personal 2x2 crafted bowls `281x4`; workbench crafted painting `321` and sign `323`.
- The stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M372 semantic SHA-256 is
`80d1b7a10efe73807810ca2609135b07e47ba57880f35e72d1b205e27394a993`.

## 1.359.0 - M371 Machine block crafts

Status: GO for official TNT, piston, and sticky-piston crafts together.

- Workbench Packet102 crafted TNT `46`, piston `33`, and sticky piston `29`.
- The stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M371 semantic SHA-256 is
`51d13daf2febf456a423e84d136707a77b9117668bc3979f4b52514bdbb26c7e`.

## 1.358.0 - M370 Remaining furnace smelts

Status: GO for official cactus, log, and clay furnace smelts together.

- Three idle furnaces `61:2` smelted cactus `81→351:2`, log `17→263:1`, and clay `337→336`.
- Cook completed at burn `1401` of coal `1600`.
- Repeated the complete family in two official server JVMs.

The frozen M370 semantic SHA-256 is
`912452d315840ced68811ccce77f3cde4f1250eac7068c5ddd9f85e22a607a2a`.

## 1.357.0 - M369 Cake full eat set

Status: GO for official cake six-slice eat through air together.

- Cake `92` was eaten `0→1→2→3→4→5→air` (six bites).
- The air cell persisted after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M369 semantic SHA-256 is
`1e7b764b96a4af45a053eec0e064137715747cb2554f80daaece626bee17a371`.

## 1.356.0 - M368 More dye wool crafts

Status: GO for official yellow, orange, and pink wool crafts together.

- White wool `35:0` plus dyes `351:11`, `351:14`, and `351:9` crafted `35:4`, `35:1`, and `35:6`.
- The stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M368 semantic SHA-256 is
`af97665c706b12d71c1c228a931a7efec0c18fda505b259de31fdf174b8a17b9`.

## 1.355.0 - M367 Piston motion set

Status: GO for official piston extend, retract, and sticky pull together.

- West-facing piston `33` extended `4->12` then retracted `12->4` with stone retained.
- Sticky piston `29` extended and pulled the same stone back.
- Repeated the complete family in two official server JVMs.

The frozen M367 semantic SHA-256 is
`eeb597ce51f18b3841a00e606375efae5dfb531672564e34670469f420f304a8`.

## 1.354.0 - M366 Map fill set

Status: GO for official empty-map Packet15 air-use together with persist.

- Empty map `358` was Packet15 air-used; the dedicated server left the stack `358:1:0`.
- The same empty map survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M366 semantic SHA-256 is
`048613204222ae9dce7fb157d74dc94b69573ce8faaa9dd90cff64f7aab8f31f`.

## 1.353.0 - M365 Compass point set

Status: GO for official held compass spawn-point needle reversal together.

- Compass `345` was Packet16-held at yaw `0` and yaw `180` with opposite needles.
- The same session stood on two spawn-bearing cells and the stack persisted.
- Repeated the complete family in two official server JVMs.

The frozen M365 semantic SHA-256 is
`45694ff4da19a7b3c554d405380e9535c075d7fee1f3880674813ed5cc9a8b68`.

## 1.352.0 - M364 Light opacity set

Status: GO for official glass, leaves, and ice sky-light nibbles together.

- Packet15 placed glass `20`, oak-backed leaves `18:8`, and ice `79`.
- Fresh-login Packet51 sky-light samples were pairwise distinct.
- Repeated the complete family in two official server JVMs.

The frozen M364 semantic SHA-256 is
`2012aa0391268a287bc772ea5a40036b761ed9f72b90d91f8462512dbb0e3fab`.

## 1.351.0 - M363 Hostile identity set

Status: GO for official zombie and skeleton Packet24 identities together.

- Two saved spawners `52` were retargeted to EntityId Zombie and Skeleton.
- After `time set 14000` the session observed Packet24 types `54` and `51`.
- Repeated the complete family in two official server JVMs.

The frozen M363 semantic SHA-256 is
`e6df497cd2826b04e3930ffb08caa875bba470b29a8b5bad4ce5cc75d48db14d`.

## 1.350.0 - M362 Fence collision set

Status: GO for official adjacent fence place plus Packet13 collision together.

- Fence item `85` placed two adjacent `85:0` cells on a raised stone fixture.
- The same Packet13 `+1 Z` step was free in air and server-corrected into the fence line.
- Repeated the complete family in two official server JVMs.

The frozen M362 semantic SHA-256 is
`5784076d8eb5c6e86478f102566067459f9c73c231b5f92141b25d65c79ae290`.

## 1.349.0 - M361 Ladder climb set

Status: GO for official two-cell ladder place plus Packet13 climb together.

- Ladder item `65` placed two east `65:5` cells on a raised stone column.
- The same session Packet13-climbed two cells of height versus the air column.
- Repeated the complete family in two official server JVMs.

The frozen M361 semantic SHA-256 is
`113dccdda9b6bd0140c7aea5b255db993bb9063c6d64ef9370f1fb9925c26340`.

## 1.348.0 - M360 Fishing catch set

Status: GO for official fishing-rod hook spawn plus raw-fish catch together.

- Fishing rod `346` used in-air (Packet15 direction 255) from a raised-stone still-water dock.
- The same session observed Packet23 type `90` then reeled Packet21 raw fish `349`.
- Repeated the complete family in two official server JVMs.

The frozen M360 semantic SHA-256 is
`b81e3dfcba437f67fee01101898bab64442120affa5b0cdb60dc16f69a2549b0`.

## 1.347.0 - M359 Bed nether explode

Status: GO for official Overworld sleep versus Nether bed explode together.

- Dimension `-1` Packet15-used a placed bed `26`; Packet17 was absent and Packet60 fired at strength `5`.
- Both bed halves were gone after the explosion and after a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M359 semantic SHA-256 is
`be77b379de881712f9089340681a1a0779977df7934e51508858f83c97a9a7a6`.

## 1.346.0 - M358 Snow craft set

Status: GO for official snow-block craft plus shovel harvest of layer and block together.

- Personal 2x2 crafted snow block `80` from four snowballs `332`.
- Gold shovel `284` broke snow layer `78` and snow block `80` to Packet21 `332`.
- Repeated the complete family in two official server JVMs.

The frozen M358 semantic SHA-256 is
`d35de53474c363b1b580a865ea4bcce9403b8f9092e3ca5be19e9f1bf6e6d1be`.

## 1.345.0 - M357 Glowstone dust crafts

Status: GO for official 2x2 glowstone-dust craft of block `89`.

- Personal 2x2 crafted glowstone `89` from four dust `348`.
- The stack survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M357 semantic SHA-256 is
`af0a81cf89ec64afd6056fb4755ef7ed9350bac34875caa333cc150d99d7955c`.

## 1.344.0 - M356 Jack-o-lantern crafts

Status: GO for official jack-o-lantern craft plus pumpkin and lantern place together.

- Personal 2x2 crafted jack-o-lantern `91` from pumpkin `86` plus torch `50`.
- Packet15 placed leftover pumpkin `86:1` and crafted lantern `91:1`; both cells survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M356 semantic SHA-256 is
`b870de18f5f7c2616c607111ea332fc3f4426f8f5a3a82d713703270066ee5b1`.

## 1.343.0 - M355 Note rest instruments

Status: GO for official glass and gold note-block instruments together.

- Packet14 on note blocks `25` over glass `20` and gold `41` emitted Packet54 instruments `3` and `0`.
- Both note cells survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M355 semantic SHA-256 is
`0b8bfa875138db6748a105c9ca98ad10bd8f4ff277dbe49e5d1d96e5790cf868`.

## 1.342.0 - M354 Farmland hydrate set

Status: GO for official dry and hydrated farmland together.

- Wooden hoe `290` tilled dirt to farmland `60`; water-adjacent plots hydrated to `60:7` while the isolated plot stayed `60:0`.
- Both dry and hydrated cells survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M354 semantic SHA-256 is
`31e18ca11dc6928034468d2a503769a4559f5757e60dffc22e8bf85af35522d2`.

## 1.341.0 - M353 Sword damage set

Status: GO for official wood, iron, and diamond sword pig hits together.

- Packet7 with swords `268`, `267`, and `276` recorded live hurts then Packet38 status 3 plus Packet29 death.
- Attacker Packet8 stayed `20`. Repeated the complete family in two official server JVMs.

The frozen M353 semantic SHA-256 is
`cfaf1e0d3a43f1bb3a09cd6dadb2462a7d953de08671761856fc1080249424e4`.

## 1.340.0 - M352 Tool durability set

Status: GO for official wooden, iron, and gold pick durability together.

- Packet14 broke cobble and stone with picks `270`, `257`, and `285`; remaining damage persisted after save.
- The three remaining held stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M352 semantic SHA-256 is
`46cbf98b50d0745eafee30276fb3d3adafbbd1381f71bf7106012dbe80b75a30`.

## 1.339.0 - M351 Painting orient set

Status: GO for official west-face and east-face painting spawns together.

- Packet15 of painting `321` hung one Packet25 on the west face (`dir1`) and one on the east face (`dir3`).
- Two peers observed matching identity and facing for both spawns.
- Repeated the complete family in two official server JVMs.

The frozen M351 semantic SHA-256 is
`8f60b715dc6a3aeab49aaae89f1f147dd7822ab37806a8da79597e86acd2e9aa`.

## 1.338.0 - M350 Sign text set

Status: GO for official standing-sign and wall-sign text together.

- Packet15 placed standing sign `63` and wall sign `68`; Packet130 wrote four lines on each tile.
- Both texts survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M350 semantic SHA-256 is
`12d6f3d9302de6833a34efdedd9599e289de1ccf722ecd4cc8e32e8fad906d79`.

## 1.337.0 - M349 Double chest set

Status: GO for official adjacent chests opening one large window together.

- Packet15 placed two neighboring chests `54`; empty-hand Packet15 opened title `Large chest` with 54 owned slots.
- Both chest cells and the 54-slot window survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M349 semantic SHA-256 is
`ec079803ad133072d794b370d1dd5988e5931287cded14a33e3abd7702c0fd26`.

## 1.336.0 - M348 Dye mix crafts

Status: GO for official orange, purple, and lime dye mixes together.

- Personal 2x2 mixed rose red, yellow, lapis, cactus green, and bone meal into `351:14`, `351:5`, and `351:10`.
- The three mixed stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M348 semantic SHA-256 is
`2c8b97b5aa9c68fef810b33465f38d10146adbfcea7c9994c7742c0ae1305b94`.

## 1.335.0 - M347 Gold diamond hoes

Status: GO for official gold hoe and diamond hoe crafts together.

- Packet102 crafted gold hoe `294` and diamond hoe `293` in a placed workbench `58`.
- Both tools survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M347 semantic SHA-256 is
`db8eed6611d61cb0063b32dcf170a0fd66d46799102196c45721842544e6515b`.

## 1.334.0 - M346 Ore block uncrafts

Status: GO for official gold, iron, diamond, and lapis block uncrafts together.

- Personal 2x2 uncrafted gold `41`, iron `42`, diamond `57`, and lapis `22` to nine-item stacks.
- The four stacks `266x9`, `265x9`, `264x9`, and `351x9:4` survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M346 semantic SHA-256 is
`6cb6facb7859e30e6d0834273f32ba84f01bede6c1d8d39ad7dcf6b33818f452`.

## 1.333.0 - M345 Ore block crafts

Status: GO for official gold, iron, diamond, and lapis block crafts together.

- Packet102 crafted gold block `41`, iron block `42`, diamond block `57`, and lapis block `22`.
- The four stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M345 semantic SHA-256 is
`1a72ee9100a460729b226ac6ea350567f9a953cb2d8832d43545f74bdf9f0427`.

## 1.332.0 - M344 Bucket fluid set

Status: GO for official water and lava bucket place-plus-pickup together.

- Water bucket `326` placed still water `9:0` then picked it back up; lava bucket `327` did the same for `11:0`.
- The empty basin and both filled buckets survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M344 semantic SHA-256 is
`fe76fdf6b8ec887d8efc4caa81ce926b3efad2a42207cbefd9b6a21f9b66b789`.

## 1.331.0 - M343 Fire family set

Status: GO for official netherrack fire persist and wool consumption together.

- Flint-and-steel `259` placed fire `51` on netherrack `87`; adjacent wool `35` was consumed.
- The netherrack flame remained after the bounded live hold and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M343 semantic SHA-256 is
`b04d10e87e540d454627a3960abbf311c9912ca625d00f3e71af970ea08e77f6`.

## 1.330.0 - M342 Gravity block set

Status: GO for official sand and gravel falls together.

- Packet14 removed stone supports so sand `12` and gravel `13` fell as Packet23 types `70` and `71`.
- Both one-cell settlements survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M342 semantic SHA-256 is
`d8653266b9cdaa16b9aa3d3fc760642400d2172380078b03499aff10394c84e8`.

## 1.329.0 - M341 Repeater delay set

Status: GO for official repeater delay bits 1 through 4 together.

- Packet15 placed west-facing repeater `93:3` and tuned delay through `93:7`, `93:11`, and `93:15`.
- The 4-tick cell `93:15` survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M341 semantic SHA-256 is
`5dfcac91e31b99f9d578961c42075eb4456a7e3dde14bf19c6d069bf7dc49136`.

## 1.328.0 - M340 Redstone input set

Status: GO for official lever latch and stone-button pulse together.

- Packet15 placed lever `69` and stone button `77`, then latched `69:1->9->1` and pulsed `77:1->9->1`.
- Both cells reloaded unpowered after a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M340 semantic SHA-256 is
`366f2922f527ce87c4818902a75b3e646c8a6e5946e6b84838fe3c9918f0c456`.

## 1.327.0 - M339 Sapling growth set

Status: GO for official oak, spruce, and birch sapling growth together.

- Bonemeal `351:15` grew saplings `6:0`, `6:1`, and `6:2` into log roots `17:0`, `17:1`, and `17:2`.
- The three log cells survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M339 semantic SHA-256 is
`cbb09ab44fa0804f8304e414f683a868c16aabac0c29c00ba78b525e6678ec5e`.

## 1.326.0 - M338 Furnace fuel set

Status: GO for official coal, plank, and lava-bucket furnace fuels together.

- Three idle furnaces `61:2` smelted cobble `4` to stone `1` with fuels `263`, `5`, and `327`.
- Packet105 burn/progress froze as `1600/1401`, `300/101`, and `20000/19801`.
- Repeated the complete family in two official server JVMs.

The frozen M338 semantic SHA-256 is
`d412ed91eacea33e26daaf3f37c6494ecb462ee19694093f7126187f36a2b957`.

## 1.325.0 - M337 Utility item crafts

Status: GO for official shears, flint-and-steel, and empty-bucket crafts together.

- Personal 2x2 crafted shears `359` and flint-and-steel `259`; workbench crafted empty bucket `325`.
- The three stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M337 semantic SHA-256 is
`24941b7cbf8eca87a6e5f03001a622de0dfb51a8d4e4f754906557bfa7603367`.

## 1.324.0 - M336 Slab meta crafts

Status: GO for official sandstone, wood, and cobble slab crafts together.

- Packet102 crafted sandstone slab `44:1`, wood slab `44:2`, and cobble slab `44:3`.
- The three stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M336 semantic SHA-256 is
`e75d0b2bb489e7ea157ad321c8dc141c57039e5411b10b96658868f3b231cc57`.

## 1.323.0 - M335 Cake slice set

Status: GO for official cake place plus three successive slices together.

- Packet15 placed uneaten cake `92:0`, then empty-hand bites advanced `0 -> 1 -> 2 -> 3`.
- The bitten `92:3` cell survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M335 semantic SHA-256 is
`3ef77cdef925e0457ef17467a33321cc83aaffe183eb51cc6fc7768273ff2f68`.

## 1.322.0 - M334 Record set

Status: GO for official gold and green disc inserts together.

- Packet15 placed two jukeboxes `84` and inserted discs `2256` and `2257`.
- Packet61 effect `1005` fired for both disc ids; both cells persisted as `84:1`.
- Repeated the complete family in two official server JVMs.

The frozen M334 semantic SHA-256 is
`b139e039c60f517453a6e8e0c3fe4f87b11f5c73faa81a77c7fceb7645428d53`.

## 1.321.0 - M333 Dispenser set

Status: GO for official dispenser place, dual load, and dual eject together.

- Packet15 placed west-facing dispenser `23:4` and a side lever on raised stone.
- Packet102 loaded cobblestone `4` and oak planks `5`; two lever pulses ejected both as Packet21.
- Repeated the complete family in two official server JVMs.

The frozen M333 semantic SHA-256 is
`46b62a083dad7f0e54a72e16e9b51144add22acb4cb53b75b51439b04385894e`.

## 1.320.0 - M332 Bow arrow set

Status: GO for official bow and arrow crafts plus Packet23 type-60 air-use.

- Packet102 crafted bow `261` and arrows `262` in a placed workbench `58`.
- Packet15 air-use of the crafted bow emitted Packet23 type `60` on the existing tracker.
- Repeated the complete family in two official server JVMs.

The frozen M332 semantic SHA-256 is
`b745e8656e459e93ffe617759990be48c4c454450256e53f8ef1c5bf1757d215`.

## 1.319.0 - M331 Throwables set

Status: GO for official snowball, egg, and fishing-hook throws together.

- Packet15 air-use emitted Packet23 types `61`, `62`, and `90` on the existing tracker.
- Two peers observed identical identity, type, and thrower for each spawn.
- Repeated the complete family in two official server JVMs.

The frozen M331 semantic SHA-256 is
`63d18b0a65f745ad18fa9a7a9e8e345e8bffe83e067224ff8687c1b03c0a7328`.

## 1.318.0 - M330 Bed sleep set

Status: GO for official bed occupy and wake together.

- Packet15 placed bed `26` foot/head, then occupied `26:8 -> 26:12` at night.
- Packet17 sleep entered the head cell; Packet70 reason stayed `-1`; skip left `26:8`.
- Repeated the complete family in two official server JVMs.

The frozen M330 semantic SHA-256 is
`1415f89a64178b9c0135d108239ba04eb9fca293f9d8ee9005347624eb6842af`.

## 1.317.0 - M329 Utility block crafts

Status: GO for official fence, ladder, and bookshelf crafts together.

- Packet102 crafted fence `85`, ladder `65`, and bookshelf `47`.
- The three stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M329 semantic SHA-256 is
`b70015b8e4bea597b4b8eeba287d216244d5c1bb9f83a1d7d06120bdb8c5086f`.

## 1.316.0 - M328 Dye family crafts

Status: GO for official dye-item crafts together.

- Packet102 crafted bone meal `351:15`, rose red `351:1`, dandelion yellow `351:11`, and gray `351:8`.
- The four dye stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M328 semantic SHA-256 is
`7ae29bcd82b147e1286ec7a3b4655087822ac5f5379f18142eab3fd163dda815`.

## 1.315.0 - M327 Food crafts

Status: GO for official stew, bread, cookie, and cake crafts together.

- Packet102 crafted stew `282`, bread `297`, cookies `357`, and cake `354`.
- Sugar `353` came from cane in the 2x2 grid; three empty buckets `325` remained.
- Repeated the complete family in two official server JVMs.

The frozen M327 semantic SHA-256 is
`feb202ff5d2172def94a39a6a9e560b5e4ecdba79681b018a8e046bb89703a54`.

## 1.314.0 - M326 Vehicle crafts

Status: GO for official boat, minecart, chest-cart, and furnace-cart crafts together.

- Packet102 crafted boat `333`, minecart `328`, chest minecart `342`, and furnace minecart `343`.
- The four result ids survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M326 semantic SHA-256 is
`1109c4ce19cf7f23d5156d80cef725329fc62a68c438e24d4294aa468e088bdc`.

## 1.313.0 - M325 Navigation crafts

Status: GO for official compass, clock, and map crafts together.

- Packet102 crafted compass `345`, clock `347`, and empty map `358`.
- The three result ids survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M325 semantic SHA-256 is
`904591e822865303c647ea818403edb8d115b37da19262cb96387da6f2e4302d`.

## 1.312.0 - M324 Furnace rest smelts

Status: GO for official remaining furnace smelts together.

- Packet105 smelted sand `12→20`, cobble `4→1`, and fish `349→350` in idle furnaces `61:2`.
- Cook `199`, burn `1600`, and completion `1401` matched across the family.
- Repeated the complete family in two official server JVMs.

The frozen M324 semantic SHA-256 is
`6c131f48c758cb5376dfd0b9504b154148f17e3295c1337c08e4c32619dc781a`.

## 1.311.0 - M323 Iron tool crafts

Status: GO for official iron-tool workbench crafts together.

- Packet102 crafted iron sword `267`, pickaxe `257`, shovel `256`, axe `258`, and hoe `292`.
- The five result ids persisted after a clean save.
- Repeated the complete family in two official server JVMs.

The frozen M323 semantic SHA-256 is
`17587efabb76f538dcae2f11569071d321ff253c298e403c9de331483b463270`.

## 1.310.0 - M322 Diamond armor crafts

Status: GO for official diamond-armor workbench crafts together.

- Packet102 crafted helmet `310`, chestplate `311`, leggings `312`, and boots `313` from diamonds `264`.
- The four result ids stayed in personal storage and armor slots 5-8 stayed empty.
- Repeated the complete family in two official server JVMs.

The frozen M322 semantic SHA-256 is
`b54acc14a0bba483871701ba342becc842fe45291b56aca8212a1b71a2b5269d`.

## 1.309.0 - M321 Gold armor crafts

Status: GO for official gold-armor workbench crafts together.

- Packet102 crafted helmet `314`, chestplate `315`, leggings `316`, and boots `317` from ingots `266`.
- The four result ids stayed in personal storage and armor slots 5-8 stayed empty.
- Repeated the complete family in two official server JVMs.

The frozen M321 semantic SHA-256 is
`a44c48c91eba492305c1faa7963dd3ad1023d9a9a97bd6ccd92c2b8abcec9fbf`.

## 1.308.0 - M320 Leather armor crafts

Status: GO for official leather-armor workbench crafts together.

- Packet102 crafted helmet `298`, chestplate `299`, leggings `300`, and boots `301` from leather `334`.
- The four result ids stayed in personal storage and armor slots 5-8 stayed empty.
- Repeated the complete family in two official server JVMs.

The frozen M320 semantic SHA-256 is
`48274c2675afd82a6d376e7ec9ceb1e8896adc3761f59461d619a2ae378b90f4`.

## 1.307.0 - M319 Stair slab crafts

Status: GO for official oak-stair, cobble-stair, and stone-slab crafts together.

- Packet102 crafted oak stairs `53`, cobble stairs `67`, and stone slab `44:0`.
- The three stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M319 semantic SHA-256 is
`cec4e38d37d31058c744ff1e9c806d2567fcf878603f2e63cdf7347058f5d553`.

## 1.306.0 - M318 Gold diamond tool crafts

Status: GO for official gold and diamond tool crafts together.

- Packet102 crafted gold tools `283,285,286,284` and diamond tools `276,278,279,277`.
- The eight result ids persisted after a clean save.
- Repeated the complete family in two official server JVMs.

The frozen M318 semantic SHA-256 is
`ea2a3772ad997141d967212b9f93a52ec0b5f633dde29b2b0192e844a377005e`.

## 1.305.0 - M317 Slow blocks

Status: GO for official cobweb and soul-sand slowdown together.

- The same Packet13 step was slower in cobweb `30:0` and on soul sand `88:0` than in air.
- Both cells survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M317 semantic SHA-256 is
`bcae75456216b2655361256edd97079669619d908394782145a21a076e9e676a`.

## 1.304.0 - M316 Shears set

Status: GO for official shears leaf and living-sheep wool together.

- Shears item `359` dropped Packet21 leaf `18` from player-placed `18:8`.
- Packet7 on a living type-91 sheep dropped Packet21 wool `35` without Packet38 status 3.
- Repeated the complete family in two official server JVMs.

The frozen M316 semantic SHA-256 is
`91eec7f3061f3c9cb956cd25ebcc7ece6a66055262a45538081a8ad72d79426e`.

## 1.303.0 - M315 Dye wool crafts

Status: GO for official 2x2 dyed-wool crafts together.

- White wool `35:0` plus dyes `351:1`, `351:2`, and `351:4` crafted `35:14`, `35:13`, and `35:11`.
- The three colored stacks survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M315 semantic SHA-256 is
`f9b78bfc4331c0fc5e92dd33443743a0c9b46e17815f9f89b20fd2535c2405d2`.

## 1.302.0 - M314 Armor crafts

Status: GO for official iron-armor workbench crafts together.

- Packet102 crafted helmet `306`, chestplate `307`, leggings `308`, and boots `309` from ingots `265`.
- The four result ids stayed in personal storage and armor slots 5-8 stayed empty.
- Repeated the complete family in two official server JVMs.

The frozen M314 semantic SHA-256 is
`914b42df18b53c2afcbb40f2f5c87b8848dc19e4e816eaef927067915c98b437`.

## 1.301.0 - M313 Note instruments

Status: GO for official note-block instruments on stone, wood, and sand together.

- Packet14 played note block `25` on stone, planks `5`, and sand `12`.
- Packet54 instruments were `1`, `4`, and `2`.
- Repeated the complete family in two official server JVMs.

The frozen M313 semantic SHA-256 is
`6e171effe14c350c22319797f836fbb498aa88b559a88bef337aa634f95943b6`.

## 1.300.0 - M312 Torch invert

Status: GO for official redstone-torch on and off together.

- Packet15 placed live torch `76:4` on stone, then a repeater inverted the same cell to `75:4`.
- Unlit `75:4` survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M312 semantic SHA-256 is
`e4b4e7bf13497288e3b90b76bd07f714f976ecc54254f40ae81e8150b4924ae9`.

## 1.299.0 - M311 Storage carts

Status: GO for official chest-minecart window and furnace-minecart spawn together.

- Chest minecart Packet23 type 11 opened Packet100 title `Minecart` with 27 slots.
- Furnace minecart Packet23 type 12 accepted Packet7 interact with no window.
- Repeated the complete family in two official server JVMs.

The frozen M311 semantic SHA-256 is
`820eecba37b12ebcd44e719255868981552e3ef995e2ba92c4df32973218a71b`.

## 1.298.0 - M310 Vehicle rides

Status: GO for official boat and minecart mount together.

- Packet23 type 1 boat and type 10 minecart were mounted with Packet39 attach.
- Actor and observer shared both vehicle ids.
- Repeated the complete family in two official server JVMs.

The frozen M310 semantic SHA-256 is
`e9490bd2395a9a0e2f23738cb8956250a2a8738d5f0d1c62c27d254b43a8ff3f`.

## 1.297.0 - M309 Rail power

Status: GO for official detector-rail occupancy and powered-rail torch power together.

- Detector rail `28` occupied by a minecart became `28:8`.
- Powered rail `27` beside redstone torch `76:5` became `27:8`.
- Repeated the complete family in two official server JVMs.

The frozen M309 semantic SHA-256 is
`ff3995ce5426f88877abdf561aada4f7f2968dfa7fbdc44f768202ec4c14ff80`.

## 1.296.0 - M308 Fragile set

Status: GO for official ice break, glass break, and ice melt together.

- Packet14 broke ice `79` to still water `9` and glass `20` to air with no glass drop.
- A second ice cell melted to still water beside torch `50:5`.
- Repeated the complete family in two official server JVMs.

The frozen M308 semantic SHA-256 is
`016e31ada167a1772c3c0ec4d610d946ddf26bc0a93c97ad494019ab72c97ce5`.

## 1.295.0 - M307 Env damage

Status: GO for official drowning, suffocation, and lava hurt together.

- Packet38 status 2 recorded drowning, falling-sand suffocation, and lava contact.
- Health `20→15` persisted across a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M307 semantic SHA-256 is
`8a51289b35f57567a0dfbc0f3cf8f1d6981dac6219b52d494aac34f56713cba7`.

## 1.294.0 - M306 Closables

Status: GO for official wooden-door and trapdoor close together.

- Empty-hand Packet15 opened then closed wooden door `64` (`0/8 → 4/12 → 0/8`).
- Empty-hand Packet15 opened then closed trapdoor `96` (`3 → 7 → 3`).
- Both closed states survived a clean save and a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M306 semantic SHA-256 is
`0287dd23ec4f04c0960b98f43f8e16ff75d416ad1fb8ffb16478c579b8bc4865`.

## 1.293.0 - M305 Plant growth

Status: GO for official wheat, cactus, and sugar-cane growth together.

- Bonemeal `351:15` forced wheat `59:0` to mature `59:7` on farmland `60`.
- Official random ticks grew cactus `81` and sugar cane `83` to height `>= 2`.
- The grown family survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M305 semantic SHA-256 is
`b755666b909da0bc4583bf752a32ff032894d3959b4dd0a47c56d3e80c066721`.

## 1.292.0 - M304 Farmland set

Status: GO for official hoe-till and farmland-trample together.

- Wooden hoe `290` tilled dirt `3` to farmland `60`, then a jump trampling restored dirt `3`.
- Unpowered dirt persisted across a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M304 semantic SHA-256 is
`ce698c2302ea621590b03877774a82c7ea0a5b085bf5536d28093462ed8c121c`.

## 1.291.0 - M303 Crop harvests

Status: GO for official wheat, sugar cane, and cactus harvests.

- Harvested mature wheat `59:7`, sugar cane `83`, and cactus `81` in one cycle.
- Packet21 drops were wheat `296`, cane `338`, and cactus `81`.
- Repeated the complete family in two official server JVMs.

The frozen M303 semantic SHA-256 is
`33bca9f328ddb3c028b792f70233157d997e260e28d47d0115069be6bcba67f0`.

## 1.290.0 - M302 Shovel soft breaks

Status: GO for official dirt, sand, gravel, and clay shovel harvests.

- Gold shovel `284` broke dirt `3`, sand `12`, gravel `13`, and clay `82`.
- Packet21 drops were dirt, sand, gravel, and clay balls `337`.
- Repeated the complete family in two official server JVMs.

The frozen M302 semantic SHA-256 is
`83e1acd8df0e978483bdfe1199d46021b2f5b8a4908c646ca1045c002e7228d9`.

## 1.289.0 - M301 Axe log breaks

Status: GO for official oak, spruce, and birch log harvests.

- Stone axe `275` broke oak `17:0`, spruce `17:1`, and birch `17:2`.
- Packet21 drops matched each log species.
- Repeated the complete family in two official server JVMs.

The frozen M301 semantic SHA-256 is
`6e62367a3c72d64d2bda9180cb0e5b0484671ef7530e74968d511330d7a06365`.

## 1.288.0 - M300 Ore pick breaks

Status: GO for official cobble, coal-ore, and diamond-ore pick harvests.

- Iron pick `257` broke cobble `4` and coal ore `16`; diamond pick `278` broke diamond ore `56`.
- Packet21 drops were cobble `4`, coal `263`, and diamond `264`.
- Repeated the complete family in two official server JVMs.

The frozen M300 semantic SHA-256 is
`5fa840f6542410b38170ae4dc2fe1d2731c8d7ca7335ba9d105d3c1feed61b1a`.

## 1.287.0 - M299 Stone tool crafts

Status: GO for official stone tool-family crafts.

- Workbench `58` crafted stone sword `272`, shovel `273`, pick `274`, axe `275`, and hoe `291`.
- All five results persisted across a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M299 semantic SHA-256 is
`c7503bc481ed407a57f6a750986b748f269a4222a4a8a2b9a3e26c5a12557c54`.

## 1.286.0 - M298 Wood tool crafts

Status: GO for official wooden tool-family crafts.

- Workbench `58` crafted wooden sword `268`, pick `270`, axe `271`, shovel `269`, and hoe `290`.
- All five results were taken and persisted across a fresh login.
- Repeated the complete family in two official server JVMs.

The frozen M298 semantic SHA-256 is
`2b099c580ef169af939546718df1c4ae560e5f875f92960733fbcc026a3982bf`.

## 1.285.0 - M297 Basic crafts

Status: GO for official 2x2 planks, sticks, and torch crafts.

- Crafted oak planks `5`, sticks `280`, and torches `50` in one personal 2x2 session.
- The result family survived inventory persistence across a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M297 semantic SHA-256 is
`f62ec64a6ea2c9990cdbf656cdedabe239862a866983d92adfb792d4f81d82a3`.

## 1.284.0 - M296 Furnace smelts

Status: GO for official iron, gold, and pork furnace recipes.

- Three idle furnaces `61:2` smelted iron ore `15` to ingot `265`, gold ore `14` to ingot `266`, and pork `319` to cooked pork `320`.
- Live cook/burn telemetry matched coal fuel (`199` / `1600` / `1401`).
- Repeated the complete family in two official server JVMs.

The frozen M296 semantic SHA-256 is
`04ac7e3f754356f848410854d026b17493e75b73dc55473905ec0d45d31787c1`.

## 1.283.0 - M295 Pressure plates

Status: GO for official wooden and stone pressure-plate power.

- Placed wooden plate `72` and stone plate `70` on adjacent raised-stone supports.
- Standing powered each cell (`72:1`, `70:1`); stepping off depowered both.
- Unpowered `72:0` and `70:0` survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M295 semantic SHA-256 is
`d36cbe38c632dcc4e03334db2982db51f92e78b36850dcbc098b43469ebb9815`.

## 1.282.0 - M294 Piston place

Status: GO for official piston placement.

- Placed piston item `33` as block `33:1` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M294 semantic SHA-256 is
`3fa31fff0d03751901d6283ff022999a5d94d205d79d1a77106294cc8b041624`.

## 1.281.0 - M293 Sticky piston place

Status: GO for official sticky piston placement.

- Placed sticky piston item `29` as block `29:1` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M293 semantic SHA-256 is
`bf6cf185cefc337d8be549efbcdce76d5c7cff54669d136c1002f30b7ff25c1e`.

## 1.280.0 - M292 Birch leaves

Status: GO for official birch leaves placement.

- Placed leaves item `18` damage `2` beside oak log as birch leaves `18:10`.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M292 semantic SHA-256 is
`909703a5406842a4c1becff13064c13eebc661300e0ebe15b3400c822771f912`.

## 1.279.0 - M291 Spruce leaves

Status: GO for official spruce leaves placement.

- Placed spruce log `17:1` beside leaves item `18` damage `1` as spruce leaves `18:9`.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M291 semantic SHA-256 is
`665728a20dbb06b11792f4b355f3b52d189d5cf5b8c0d06099db1447b1b7f0d5`.

## 1.278.0 - M290 Birch sapling

Status: GO for official birch sapling placement.

- Planted sapling item `6` damage `2` as birch sapling `6:2` on dirt over raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M290 semantic SHA-256 is
`21f35395f38d2877297a2801023c0e7e0e0b5fc83a8ec278dee1ad7b7151b8a0`.

## 1.277.0 - M289 Spruce sapling

Status: GO for official spruce sapling placement.

- Planted sapling item `6` damage `1` as spruce sapling `6:1` on dirt over raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M289 semantic SHA-256 is
`338c07cf0cc26fad4d048f900242741d71662eac8c8f48d98d41ede8c541dc2c`.

## 1.276.0 - M288 Brown wool

Status: GO for official brown wool placement.

- Placed wool item `35` damage `12` as brown wool `35:12` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M288 semantic SHA-256 is
`98dd28d183167f3553fde2b22ed4f84c648da9eb7ba620ff9ec066c95de722a8`.

## 1.275.0 - M287 Purple wool

Status: GO for official purple wool placement.

- Placed wool item `35` damage `10` as purple wool `35:10` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M287 semantic SHA-256 is
`5dc40bd722b0e06eda7a5458a94b93ca0bdccfc730d99fdbe3204f19d850a7a8`.

## 1.274.0 - M286 Cyan wool

Status: GO for official cyan wool placement.

- Placed wool item `35` damage `9` as cyan wool `35:9` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M286 semantic SHA-256 is
`1632e3056edc9c3fa6a76285a528128698313979964d10cedb2b03544c838e61`.

## 1.273.0 - M285 Light gray wool

Status: GO for official light-gray wool placement.

- Placed wool item `35` damage `8` as light-gray wool `35:8` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M285 semantic SHA-256 is
`f98cb91704701be85feaa966d2fbe24aa8b5b4df58daeabe7fc7a799836f7ae5`.

## 1.272.0 - M284 Gray wool

Status: GO for official gray wool placement.

- Placed wool item `35` damage `7` as gray wool `35:7` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M284 semantic SHA-256 is
`73e9c154cc10de9ba90cb2af73ce28ad87ed76e593fc4961f12616d08161821c`.

## 1.271.0 - M283 Pink wool

Status: GO for official pink wool placement.

- Placed wool item `35` damage `6` as pink wool `35:6` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M283 semantic SHA-256 is
`54b69bd3555ea1c71c7bfe4a627ef3aebad41301df717ac15f1338148b198863`.

## 1.270.0 - M282 Lime wool

Status: GO for official lime wool placement.

- Placed wool item `35` damage `5` as lime wool `35:5` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M282 semantic SHA-256 is
`9b8eadf13d246083c150829c5914921e95f5f55d4dadc11e35ae365d392615c6`.

## 1.269.0 - M281 Light blue wool

Status: GO for official light-blue wool placement.

- Placed wool item `35` damage `3` as light-blue wool `35:3` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M281 semantic SHA-256 is
`49e519da435b759ce7053b4105c826cbae35a31badd8bc4f4e50d0cd48617e1f`.

## 1.268.0 - M280 Magenta wool

Status: GO for official magenta wool placement.

- Placed wool item `35` damage `2` as magenta wool `35:2` on raised stone.
- The exact cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M280 semantic SHA-256 is
`1c2065b1a6b6a8fdbe04e1a4ed0e9d52b6fa44e7fc16fd931de75c89e017e1fe`.

## 1.267.0 - M279 Button press

Status: GO for official stone-button pulse.

- Placed east-face button `77:1` then empty-hand Packet15 pulsed it to `77:9` and back to `77:1`.
- The released `77:1` cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M279 semantic SHA-256 is
`910692630d2dc89d5acd515f421970042c6dd218a9f6b2fbc97883e672bd3eb7`.

## 1.266.0 - M278 Trapdoor toggle

Status: GO for official trapdoor open-bit toggle.

- Placed trapdoor `96:3` then empty-hand Packet15 opened it to `96:7`.
- The open metadata survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M278 semantic SHA-256 is
`a66063d6e5ac041de1eeb23cf5a56d2fe303a9759694e0dd69ce31347ef8442a`.

## 1.265.0 - M277 Wooden door open

Status: GO for official wooden-door toggle.

- Placed wooden door `64:0` / `64:8` then empty-hand Packet15 opened both halves to `64:4` / `64:12`.
- The open pair survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M277 semantic SHA-256 is
`1f0b2fd8a64b2092de4a093f2d5cf0c8110b4363e2ee0199faf1ca2ae7ff2eb0`.

## 1.264.0 - M276 Fire damage

Status: GO for official netherrack-fire contact damage.

- Stood in fire `51:0` on netherrack `87:0` so Packet8 health dropped `20 -> 19`.
- The flame cell and the one-point hurt survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M276 semantic SHA-256 is
`6df6fefaf368f9cde54b95ead0d046469348c56c0bc386f4306b0d0a5a14a043`.

## 1.263.0 - M275 Cactus damage

Status: GO for official cactus contact damage.

- Placed cactus `81:0` on sand and walked into it so Packet8 health dropped `20 -> 19`.
- Packet38 status 2 accompanied the one-point hurt and both sides survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M275 semantic SHA-256 is
`c708ae878b6079760d5c246f952ca1789d98c31e395a568ad9c1a2d751ef6df8`.

## 1.262.0 - M274 Falling gravel

Status: GO for official one-cell falling gravel.

- Removed the stone support under gravel `13:0` and settled the official `1:0 -> 13:0` / `13:0 -> 0:0` pair.
- Exactly two complete-chunk states differed after forty gravity ticks and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M274 semantic SHA-256 is
`176ae1fac3a1eb0fc755149f750defb1e9bf184c097416e0d6f216e41c7fb222`.

## 1.261.0 - M273 Chain boots

Status: GO for official chain-boots equipment.

- Window-0 click moved chain boots `305` into armor slot 8; peer Packet5 slot 1 showed `305`.
- The piece is distinct from leather `301` and survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M273 semantic SHA-256 is
`509d729ffedcf64fb1478260c71654e80183c1936480c9db878459abe189ec16`.

## 1.260.0 - M272 Diamond leggings

Status: GO for official diamond-leggings equipment.

- Window-0 click moved diamond leggings `312` into armor slot 7; peer Packet5 slot 2 showed `312`.
- The piece is distinct from leather `300` and survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M272 semantic SHA-256 is
`84c5aee2ec930400dffd8c3fb58277fed715027fa6a538ea53d5acc3cd24b9a0`.

## 1.259.0 - M271 Gold chestplate

Status: GO for official gold-chestplate equipment.

- Window-0 click moved gold chestplate `315` into armor slot 6; peer Packet5 slot 3 showed `315`.
- The piece is distinct from leather `299` and iron `307` and survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M271 semantic SHA-256 is
`c494170f3fb2b9f4b9ec515518081834f5fe6ccd52977a7bb7e82fc946101fea`.

## 1.258.0 - M270 Iron helmet

Status: GO for official iron-helmet equipment.

- Window-0 click moved iron helmet `306` into armor slot 5; peer Packet5 slot 4 showed `306`.
- The piece is distinct from leather helmet `298` and survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M270 semantic SHA-256 is
`d62f78b5a3cb690f1845fa802de6bfa0cca27bc60ed090c60f93fdc665bf4f07`.

## 1.257.0 - M269 Shears leaves

Status: GO for official shears-versus-bare-hand leaf harvest.

- Shears item `359` on oak leaves `18:8` emitted Packet21 leaf drop `18`; bare hand did not.
- Both sheared and bare-hand cells became air `0:0` after the official break.
- Repeated the complete fixture in two official server JVMs.

The frozen M269 semantic SHA-256 is
`ea2a38e965fa7a2b9bf0d278cfd600efa328aba31fa9c3c991c85eb408a2953e`.

## 1.256.0 - M268 Flint steel fire

Status: GO for official flint-and-steel fire placement.

- Used flint-and-steel `259` on raised stone and received fire block `51:0`.
- The exact fire cell survived a clean save and a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M268 semantic SHA-256 is
`e73f7f6c77c41d2facb9ca438c3905559515101c6dcdbe3cfd22c4b48da0aeda`.

## 1.255.0 - M267 Milk bucket

Status: GO for official milk-bucket air-use consume.

- Seeded milk bucket `335` and Packet15 air-use left empty bucket `325` with no health change.
- The official Beta 1.7.3 milk use is a consume, not a heal; both held item and health survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M267 semantic SHA-256 is
`08a4bde6b39728f2585676409fd066a93aea351c0b561981b8e29c9fc7a2cff8`.

## 1.254.0 - M266 Cooked fish eat

Status: GO for official cooked-fish air-use heal.

- Seeded cooked fish `350` at health `15` and Packet15 air-use restored Packet8 `15 -> 20`.
- The held stack was consumed `350:1 -> 0` and both health and inventory survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M266 semantic SHA-256 is
`6a35349bc3363e2a0bdcba540cf2da951f99fef652b0ebf654ed56e15f0e168f`.

## 1.253.0 - M265 Fish eat

Status: GO for official raw-fish air-use heal.

- Seeded raw fish `349` at health `18` and Packet15 air-use restored Packet8 `18 -> 20`.
- The held stack was consumed `349:1 -> empty` and both health and inventory survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M265 semantic SHA-256 is
`0c9b15289f11f60a602735efc2cf64ae7cf2e4ad6454e33fd5fdb6a44023f832`.

## 1.252.0 - M264 Raw pork eat

Status: GO for official raw-porkchop air-use heal.

- Seeded raw porkchop `319` at health `17` and Packet15 air-use restored Packet8 `17 -> 20`.
- The held stack was consumed `319:1 -> 0` and both health and inventory survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M264 semantic SHA-256 is
`c43583070f3c4185f97ebefa6a8f0a6ce3428f70ea6621d094546db1cbae4502`.

## 1.251.0 - M263 Stew eat

Status: GO for official mushroom-stew air-use heal.

- Seeded mushroom stew `282` at health `12` and Packet15 air-use restored Packet8 `12 -> 20`.
- The held stack became bowl `281:1` and both health and inventory survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M263 semantic SHA-256 is
`94038e1a1f75ad42e97730c63d6089ab182511bd6f5889d8a1610d83e5471bc9`.

## 1.250.0 - M262 Cookie eat

Status: GO for official cookie air-use heal.

- Seeded cookie `357` at health `19` and Packet15 air-use restored Packet8 `19 -> 20`.
- The held stack was consumed `357:1 -> empty` and both health and inventory survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M262 semantic SHA-256 is
`2374d08bdc38c0697e31d3009c028cbb6f70fb2794e2d20606804aaa4dbfb0bc`.

## 1.249.0 - M261 Golden apple eat

Status: GO for official golden-apple air-use heal.

- Seeded golden apple `322` at health `10` and Packet15 air-use restored Packet8 `10 -> 20`.
- The held stack was consumed `322:1:0 -> empty` and both health and inventory survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M261 semantic SHA-256 is
`888e7fe8215ede44deaf9a73fa95ecc769f61e554e9262d0781ae75eca2e9fe3`.

## 1.248.0 - M260 Apple eat

Status: GO for official apple air-use heal.

- Seeded apple `260` at health `16` and Packet15 air-use restored Packet8 `16 -> 20`.
- The held stack was consumed `260:1:0 -> empty` and both health and inventory survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M260 semantic SHA-256 is
`f5122f857247406ea443e90df7cb0b2f8b8bfd0ef8f151b677d9b3f8a4598130`.

## 1.247.0 - M259 Cooked pork eat

Status: GO for official cooked-pork air-use heal.

- Seeded cooked pork `320` at health `12` and Packet15 air-use restored Packet8 `12 -> 20`.
- The held stack was consumed `320:1:0 -> empty` and both health and inventory survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M259 semantic SHA-256 is
`c287f963780070b55c9773bcc0ad5b914a8c6a7713870dd9d5533eda3d449b0e`.

## 1.246.0 - M258 Bread eat

Status: GO for official bread air-use heal.

- Seeded bread `297` at health `15` and Packet15 air-use restored Packet8 `15 -> 20`.
- The held stack was consumed `297:1 -> 0` and both health and inventory survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M258 semantic SHA-256 is
`1f0cbe46155bbaf393891dc8f4343effa6b5f502c8efc4c8f0122c424a05da3a`.

## 1.245.0 - M257 Furnace minecart

Status: GO for official furnace-minecart spawn.

- Used furnace-minecart item `343` on rail `66` and received Packet23 type `12`.
- Two peers shared the same object identity and thrower `0`.
- Repeated the complete fixture in two official server JVMs.

The frozen M257 semantic SHA-256 is
`57acb0174de88e73ae6725e8a676aa2dffb0d4b73fe19cbe462be5d882a70264`.

## 1.244.0 - M256 Chest minecart

Status: GO for official chest-minecart spawn.

- Used chest-minecart item `342` on rail `66` and received Packet23 type `11`.
- Two peers shared the same object identity and thrower `0`.
- Repeated the complete fixture in two official server JVMs.

The frozen M256 semantic SHA-256 is
`77d7cc9f33cf75c87ba161f4e0b38376562e8c3a4a1bed0d9a78aaca8f9d0a74`.

## 1.243.0 - M255 Lava place

Status: GO for official still-lava placement from a lava bucket.

- Placed lava bucket `327` into a raised stone basin as still lava `11:0`.
- The held stack became empty bucket `325` and both sides survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M255 semantic SHA-256 is
`62a96d88efc9d70f9cef9dc52f4555dbbaf332fb3b578a2c210abfa722ade72d`.

## 1.242.0 - M254 Water place

Status: GO for official still-water placement from a water bucket.

- Placed water bucket `326` into a raised stone basin as still water `9:0`.
- The held stack became empty bucket `325` and both sides survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M254 semantic SHA-256 is
`7349aeca365432ce5e3996f11aa401973978fa83d7fe895578999c7cd306cac2`.

## 1.241.0 - M253 Green wool

Status: GO for official green-wool placement.

- Placed wool item `35` damage `13` on a raised stone support as `35:13`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M253 semantic SHA-256 is
`dafe191ebebe5c80fb8935d352a73bebfb997614628047215ffde468c90be210`.

## 1.240.0 - M252 Blue wool

Status: GO for official blue-wool placement.

- Placed wool item `35` damage `11` on a raised stone support as `35:11`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M252 semantic SHA-256 is
`9df82196251a63865e986ad531cff422ad86ee987524f8c25679c38143ac80a3`.

## 1.239.0 - M251 Black wool

Status: GO for official black-wool placement.

- Placed wool item `35` damage `15` on a raised stone support as `35:15`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M251 semantic SHA-256 is
`b046e1d6723ba4e19db7d84e49aafbd1dd701fc696e2ce2f6754ad839f4a23be`.

## 1.238.0 - M250 Red wool

Status: GO for official red-wool placement.

- Placed wool item `35` damage `14` on a raised stone support as `35:14`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M250 semantic SHA-256 is
`c3fcc1daa3851d1bcf11abcdee87a5fa5a626dc413d114ba6ffa58c0692ef726`.

## 1.237.0 - M249 Yellow wool

Status: GO for official yellow-wool placement.

- Placed wool item `35` damage `4` on a raised stone support as `35:4`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M249 semantic SHA-256 is
`1aa0065907c89647235eddd412bad95e322d6ecd1ecfdb97dfdd1a8a7f20e599`.

## 1.236.0 - M248 Orange wool

Status: GO for official orange-wool placement.

- Placed wool item `35` damage `1` on a raised stone support as `35:1`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M248 semantic SHA-256 is
`89d3e625a2e980af7b569af8fc82e46b0e5ecff6f79e8090cabff02a38496590`.

## 1.235.0 - M247 Birch log

Status: GO for official birch-log placement.

- Placed log item `17` damage `2` on a raised stone support as `17:2`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M247 semantic SHA-256 is
`d2edaaf83b9d8b74ec8d46d00e40f224fc12335a5a7e9fd35e7744a490781eb0`.

## 1.234.0 - M246 Spruce log

Status: GO for official spruce-log placement.

- Placed log item `17` damage `1` on a raised stone support as `17:1`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M246 semantic SHA-256 is
`da7cf603b820a91005a39a8dcd6ce70f9779f145f26a8ffc835f7ad93a077693`.

## 1.233.0 - M245 Wall sign

Status: GO for official wall-sign placement and Packet130 text.

- Placed sign item `323` on an east face as wall sign `68:5`.
- Four UCS-2 lines `Wall` / `sign` / `M245` / `ok` survived a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M245 semantic SHA-256 is
`124ca56d12f9c02d6f8463c6ad28739dcc1c7b29875b4fa356570082c5f82c06`.

## 1.232.0 - M244 Cake place

Status: GO for official uneaten cake placement.

- Placed cake item `354` on a raised stone support as `92:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M244 semantic SHA-256 is
`3c7fa802b04a3eee353f1129e19f03bc24418ba2dfdab4b7598ebb480edc23fd`.

## 1.231.0 - M243 Redstone wire

Status: GO for official unpowered redstone-wire placement.

- Placed redstone dust item `331` on a raised stone support as `55:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M243 semantic SHA-256 is
`6df39ca0f09d2fc710e2636bbad733e4c6e4b94ce946e915ac6367443b22a45f`.

## 1.230.0 - M242 Lever place

Status: GO for official lever placement.

- Placed lever item `69` on an east face as `69:1`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M242 semantic SHA-256 is
`28f6b893342410779d684e2473574c663af5c667ff857e946aa225e71f8f69bf`.

## 1.229.0 - M241 Iron door place

Status: GO for official iron-door placement.

- Placed iron door item `330` as lower `71:0` and upper `71:8`.
- Those cells survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M241 semantic SHA-256 is
`a4f2a5f71fe45b70e843d41f32d6a4327eac0d654a488b059ca29eb2a2d261e6`.

## 1.228.0 - M240 Bed place

Status: GO for official two-block bed placement.

- Placed bed item `355` as foot `26:0` and head `26:8` from look yaw `0`.
- Those cells survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M240 semantic SHA-256 is
`366879f4dbd3ab1b199692d6094ad12c0ee76aa41469342f4ee3fba3d74ec59d`.

## 1.227.0 - M239 Sand

Status: GO for official supported-sand placement.

- Placed sand item `12` on a raised stone support as `12:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M239 semantic SHA-256 is
`bb15230ca24298e16113b08fd83f225bd6b7966fbcdd78d7378ecc59c58e1094`.

## 1.226.0 - M238 Grass

Status: GO for official grass-block placement.

- Placed grass item `2` on a raised stone support as `2:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M238 semantic SHA-256 is
`3056478f663744b460245599a51d711c0d26a0d619c4595bd2def1f6cf3f99d4`.

## 1.225.0 - M237 Stone

Status: GO for official stone placement.

- Placed stone item `1` on a raised stone support as `1:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M237 semantic SHA-256 is
`a8969e296f04e9e9e445c08139a0fd689dc08bc6796515a90ad78d5b8e4f3ee9`.

## 1.224.0 - M236 Cobble slab

Status: GO for official cobblestone-slab placement.

- Placed slab item `44` damage `3` on a raised stone support as `44:3`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M236 semantic SHA-256 is
`0659df1d047139297efabd6988985a6f3c1bb9b16e6dfa93f6bc1387d8dbc335`.

## 1.223.0 - M235 Wood slab

Status: GO for official wood-slab placement.

- Placed slab item `44` damage `2` on a raised stone support as `44:2`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M235 semantic SHA-256 is
`db22e6770987d78c1a09404991df3d73037770d68f7fb9215a20f0e9a4f383fa`.

## 1.222.0 - M234 Sandstone slab

Status: GO for official sandstone-slab placement.

- Placed slab item `44` damage `1` on a raised stone support as `44:1`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M234 semantic SHA-256 is
`607498ca23a0859d9a2296eba8fb8d4c4c407a3af84801eb6ccf552314e65f2a`.

## 1.221.0 - M233 Note block place

Status: GO for official note-block placement.

- Placed note block item `25` on a raised stone support as `25:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M233 semantic SHA-256 is
`7b80c1a46b0ca115b927e8ef216452351d1c9bcef2ca26a49e5ab4dc6abedcc9`.

## 1.220.0 - M232 Chest place

Status: GO for official chest placement.

- Placed chest item `54` on a raised stone support as `54:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M232 semantic SHA-256 is
`aede6e11abbf46c3049a99931cbbaa22b00fa2ee80c21bca48aa57796ee2d1b9`.

## 1.219.0 - M231 Dispenser place

Status: GO for official dispenser placement.

- Placed dispenser item `23` on a raised stone support as `23:3` from look yaw `180`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M231 semantic SHA-256 is
`0679d7a20880a59f567737898c4d0263b285b13b738810981188b7b8b8fbecf4`.

## 1.218.0 - M230 Lapis ore

Status: GO for official lapis-ore placement.

- Placed lapis ore item `21` on a raised stone support as `21:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M230 semantic SHA-256 is
`f684efb5f52db98a991a7edd381072bb58cc997a3d3bfd07327725c2e5026139`.

## 1.217.0 - M229 Redstone ore

Status: GO for official unlit redstone-ore placement.

- Placed redstone ore item `73` on a raised stone support as `73:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M229 semantic SHA-256 is
`7d3985f4b7402dfe18498e350a010e0aa42df1a87bbb12ac06d7ff9bc4803504`.

## 1.216.0 - M228 Diamond ore

Status: GO for official diamond-ore placement.

- Placed diamond ore item `56` on a raised stone support as `56:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M228 semantic SHA-256 is
`d8899da5d17c18d27351804645eead6a4a792f1d0497886d4bd24a15bedef72b`.

## 1.215.0 - M227 Gold ore

Status: GO for official gold-ore placement.

- Placed gold ore item `14` on a raised stone support as `14:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M227 semantic SHA-256 is
`0cc34fc524f7aba7d51b5f354569bbbfa7bae8bde9995797972fac9dea8ba1fd`.

## 1.214.0 - M226 Iron ore

Status: GO for official iron-ore placement.

- Placed iron ore item `15` on a raised stone support as `15:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M226 semantic SHA-256 is
`a0e1b3a34325710f72942c88c73bd15a8cad197fb7b04c8bbfc5f867b97389b7`.

## 1.213.0 - M225 Coal ore

Status: GO for official coal-ore placement.

- Placed coal ore item `16` on a raised stone support as `16:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M225 semantic SHA-256 is
`2ff6ed07ba198e90c82b36fb04ced28cc6e6900e1ccca71d4ee74554803e53fd`.

## 1.212.0 - M224 Netherrack

Status: GO for official Overworld netherrack placement.

- Placed netherrack item `87` on a raised stone support as `87:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M224 semantic SHA-256 is
`790c2fc12c97371a7b0a14f5a41376c1d23f3bd1fff998120baed91087fd917b`.

## 1.211.0 - M223 Dirt

Status: GO for official dirt placement.

- Placed dirt item `3` on a raised stone support as `3:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M223 semantic SHA-256 is
`cb51b4a07fd7c818ad09e7ea60fe06e3c01a3f3bca23a3c618cfe10d5a5cb650`.

## 1.210.0 - M222 Cobble

Status: GO for official cobblestone placement.

- Placed cobblestone item `4` on a raised stone support as `4:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M222 semantic SHA-256 is
`b85fbf8097da42d8a630be610e6ab4457bd0de302e1f3396daff2d3fd161ac20`.

## 1.209.0 - M221 Furnace

Status: GO for official idle-furnace placement.

- Placed furnace item `61` on a raised stone support as `61:2`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M221 semantic SHA-256 is
`88ee8a957cbe3c4c1f6f27f8b4fa73786709b04be0e37ea4c9b50e01737b965c`.

## 1.208.0 - M220 Workbench

Status: GO for official workbench placement.

- Placed workbench item `58` on a raised stone support as `58:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M220 semantic SHA-256 is
`75f1f8dccff9989eba1c5ac186c1f62a8054452d6d6ebe7c59e09de2742a37ed`.

## 1.207.0 - M219 TNT place

Status: GO for official unprimed TNT placement.

- Placed TNT item `46` on a raised stone support as `46:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M219 semantic SHA-256 is
`4f2b9d0bc3bbd9a35010f5efbb02cf5d5e290dcfb590633b35833a02551912f8`.

## 1.206.0 - M218 Gravel

Status: GO for official supported-gravel placement.

- Placed gravel item `13` on a raised stone support as `13:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M218 semantic SHA-256 is
`3e2635199e586e6323b8da68ffe023b77a589a27d2262ed9aa0f1dc79e604e06`.

## 1.205.0 - M217 Mossy cobble

Status: GO for official mossy-cobblestone placement.

- Placed mossy cobblestone item `48` on a raised stone support as `48:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M217 semantic SHA-256 is
`e7a7a8e7b99473c5e1e123d0d2542636867e0e5ebae855883295e487ee937a43`.

## 1.204.0 - M216 Obsidian

Status: GO for official inventory obsidian placement.

- Placed obsidian item `49` on a raised stone support as `49:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M216 semantic SHA-256 is
`37b0c135b01ef8c65cbbb0636a135f26610fb0825a3acf378e396d5150a32bce`.

## 1.203.0 - M215 Lapis block

Status: GO for official lapis-block placement.

- Placed lapis lazuli block item `22` on a raised stone support as `22:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M215 semantic SHA-256 is
`2eccf7bc9e04af5137d3804025114bea5686598ee5995596d8392a2a88b7fdbf`.

## 1.202.0 - M214 Diamond block

Status: GO for official diamond-block placement.

- Placed diamond block item `57` on a raised stone support as `57:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M214 semantic SHA-256 is
`8915ac3e6a21fca55c8386b8affce516f9b29aa3d4fcb00dca48fec0fb8f4eed`.

## 1.201.0 - M213 Iron block

Status: GO for official iron-block placement.

- Placed iron block item `42` on a raised stone support as `42:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M213 semantic SHA-256 is
`f08a9fd9455cea30e230862721b12da696334532f35e67d3bcc977f3154ca81d`.

## 1.200.0 - M212 Gold block

Status: GO for official gold-block placement.

- Placed gold block item `41` on a raised stone support as `41:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M212 semantic SHA-256 is
`c41efdd35e74da0cb05078664f13008bcf8d47032c07fa0360ff0f4e57b9a9ce`.

## 1.199.0 - M211 Double slab

Status: GO for official double stone-slab placement.

- Placed double slab item `43` on a raised stone support as `43:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M211 semantic SHA-256 is
`a5ad4aa55f65cbcb979ce018f5143d7d2036b7db34c77ed5f1369e2624fbf546`.

## 1.198.0 - M210 Oak planks

Status: GO for official oak-plank placement.

- Placed planks item `5` on a raised stone support as `5:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M210 semantic SHA-256 is
`00344a185d84b7fb4fd15968e5ef176dc91d8034d17cada76115ac37d3d437f1`.

## 1.197.0 - M209 Leaves

Status: GO for official oak-leaf placement with an adjacent log.

- Placed oak log `17:0` beside leaves `18:8` so the decay-check bit survives.
- That leaf cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M209 semantic SHA-256 is
`aad065fc1b2eee7b0366a5095df49496d2002a0991c39ffefad789bb6896d5bd`.

## 1.196.0 - M208 Oak log

Status: GO for official oak-log placement.

- Placed log item `17` on a raised stone support as upright oak `17:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M208 semantic SHA-256 is
`c371df4ca97d388218b9184b3b6a0ba2745803de01514b4b8562c6ca33b533d2`.

## 1.195.0 - M207 Sandstone

Status: GO for official sandstone placement.

- Placed sandstone item `24` on a raised stone support as `24:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M207 semantic SHA-256 is
`f1168be76eb874a213a7c540fcc667aeb929883a30fe9ccb00676cf74cf65b8e`.

## 1.194.0 - M206 Sponge

Status: GO for official sponge placement.

- Placed sponge item `19` on a raised stone support as `19:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M206 semantic SHA-256 is
`f403158d252cd74d08246cef3eee7b0ea15f96a1c88448ec6ce62d608093a441`.

## 1.193.0 - M205 Brick

Status: GO for official brick-block placement.

- Placed brick item `45` on a raised stone support as `45:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M205 semantic SHA-256 is
`e086305d19013c746cd1e24ea91a5cf8c20cb87ac8b3e0d6c79d11b224a1ac90`.

## 1.192.0 - M204 Clay

Status: GO for official clay-block placement.

- Placed clay item `82` on a raised stone support as `82:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M204 semantic SHA-256 is
`3b76da4b3617c14a154aaf9799e7cd86631ba7e4e9be1d8dd1baf628fb271d8c`.

## 1.191.0 - M203 Snow layer

Status: GO for official single snow-layer placement.

- Placed snow layer item `78` on a raised stone support as `78:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M203 semantic SHA-256 is
`23163bc78dd8ce876aff379a6292ad170fe0b74e5c26f2ee7177508222aa0178`.

## 1.190.0 - M202 Sapling

Status: GO for official oak-sapling placement on dirt.

- Placed dirt `3` then oak sapling `6` as `6:0`.
- That plant cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M202 semantic SHA-256 is
`7772115ec090ef211b01204fa558371ea9983994367b0ceb0899a44441bdb24d`.

## 1.189.0 - M201 Red mushroom

Status: GO for official red-mushroom placement in a dark pocket.

- Placed dirt under a one-block stone roof, then red mushroom `40` as `40:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M201 semantic SHA-256 is
`26885845d6ad7c99bd324b497cb5415fe9056a79b295d940bc26da6049f0848a`.

## 1.188.0 - M200 Brown mushroom

Status: GO for official brown-mushroom placement in a dark pocket.

- Placed dirt under a one-block stone roof, then brown mushroom `39` as `39:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M200 semantic SHA-256 is
`4140e189cc2fa3f53a899ffa1b8332f24d0bf2a320fc2dc5f0050e9026718c70`.

## 1.187.0 - M199 Rose

Status: GO for official red-rose placement on dirt.

- Placed dirt `3` on the raised stone fixture, then rose `38` as `38:0`.
- That plant cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M199 semantic SHA-256 is
`d79a60342cee16cfece9348ecc6371263bd13bc5f50896d336b8fae6d9d750dd`.

## 1.186.0 - M198 Dandelion

Status: GO for official yellow-flower placement on dirt.

- Placed dirt `3` on the raised stone fixture, then yellow flower `37` as `37:0`.
- That plant cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M198 semantic SHA-256 is
`616709e090098e93e2e1928b9cde1a0122d5145752b870feee73266b32ce82cd`.

## 1.185.0 - M197 Wool

Status: GO for official white-wool placement.

- Placed white wool item `35` on a raised stone support as `35:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M197 semantic SHA-256 is
`b6f11e7750a8b8ece43e987d07cc4862caf7e0e44f636b6ffb32f68b0601e8f6`.

## 1.184.0 - M196 Glass

Status: GO for official glass placement.

- Placed glass item `20` on a raised stone support as `20:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M196 semantic SHA-256 is
`559e9d38c638c27b461ef834cc558063696964fbdff90e68a729877cb3daa13e`.

## 1.183.0 - M195 Cobweb

Status: GO for official cobweb placement.

- Placed cobweb item `30` on a raised stone support as `30:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M195 semantic SHA-256 is
`4ebaa9934454eff6fdfeec745c7a31c093ebc734fd77ade5eac818c4c3c8531a`.

## 1.182.0 - M194 Snow block

Status: GO for official snow-block placement.

- Placed snow block item `80` on a raised stone support as `80:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M194 semantic SHA-256 is
`27b0f59762b6d741c75eb15488c5800f88feb7ca971582557e47532e6cc98a83`.

## 1.181.0 - M193 Ice

Status: GO for official ice placement.

- Placed ice item `79` on a raised stone support as `79:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M193 semantic SHA-256 is
`928502e6104af660eee12a0404bcc27b28b4d98e8da3440ba59e805f615f5c2a`.

## 1.180.0 - M192 Soul sand

Status: GO for official Overworld soul-sand placement.

- Placed soul sand item `88` on a raised stone support as `88:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M192 semantic SHA-256 is
`4cf9190cca5bf84eabf13581b40e3e944a7c81c6c62f80bea686b7ff436ea63e`.

## 1.179.0 - M191 Glowstone

Status: GO for official Overworld glowstone placement.

- Placed glowstone item `89` on a raised stone support as `89:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M191 semantic SHA-256 is
`3b17d9813ae06da188a84e5b3ea33feedbc0bc0f9c537be65e90e2dbf47f2187`.

## 1.178.0 - M190 Jack-o-lantern

Status: GO for official jack-o-lantern facing.

- Placed jack-o-lantern item `91` with look yaw `-90` as `91:1`.
- That facing survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M190 semantic SHA-256 is
`6d925e40f9f78a804de2b69ee8eb5107d5314dedb78a27dd72a2acb8fd53f77d`.

## 1.177.0 - M189 Bookshelf

Status: GO for official bookshelf placement.

- Placed bookshelf item `47` on a raised stone support as `47:0`.
- That cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M189 semantic SHA-256 is
`30ee0483e42551b855a4fd5a85002dc5871168bc8f2bed26ec7dcd572b2b97a3`.

## 1.176.0 - M188 Stone slab

Status: GO for official single stone-slab placement.

- Placed stone slab item `44` on a raised stone support as `44:0`.
- That single-slab cell survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M188 semantic SHA-256 is
`58238d33d76d1cd336bbd528ecfe662b6e821d1118c643e6c3023f1b18c800f1`.

## 1.175.0 - M187 Cobble stairs

Status: GO for official cobble-stairs facing.

- Placed cobblestone stairs item `67` with look yaw `-90` as east-facing `67:0`.
- That facing survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M187 semantic SHA-256 is
`a569478836d30464768b7bc64d771b5cd735b7fb65e33ab5bb5a661e1f318a96`.

## 1.174.0 - M186 Oak stairs

Status: GO for official oak-stairs facing.

- Placed wooden stairs item `53` with look yaw `-90` as east-facing `53:0`.
- That facing survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M186 semantic SHA-256 is
`2af3b75842248ca27774c84635352ca60069d3ae89a045bfbe93d37a926c2ccd`.

## 1.173.0 - M185 Detector rail

Status: GO for official unpowered detector-rail placement.

- Placed detector rail item `28` on a raised stone support as `28:0`.
- Detector bit stayed 0 without a minecart and persisted after save plus login.
- Repeated the complete fixture in two official server JVMs.

The frozen M185 semantic SHA-256 is
`81cc57ce0d8d5c637a58696af5d3d47097bd3d14016813703c4f7718cb9505a2`.

## 1.172.0 - M184 Powered rail

Status: GO for official unpowered powered-rail placement.

- Placed powered rail item `27` on a raised stone support as unpowered `27:0`.
- Powered bit 8 stayed 0 without redstone and persisted after save plus login.
- Repeated the complete fixture in two official server JVMs.

The frozen M184 semantic SHA-256 is
`d017b9f94e15a87ad2465679091958308c683a77948158ee7b9b3fb241c56264`.

## 1.171.0 - M183 Rails

Status: GO for official north-south rail placement.

- Placed rail item `66` on a raised stone support as `66:0` with look yaw `0`.
- That facing survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M183 semantic SHA-256 is
`189c56b02557d604b8886acbf0eb51505d54eab8cedbb903987462eebb7b3c46`.

## 1.170.0 - M182 Redstone torch

Status: GO for official floor redstone-torch placement.

- Placed redstone torch item `76` on a raised stone support as `76:5`.
- That floor metadata survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M182 semantic SHA-256 is
`3e8129618cb8674fb0d5a7580c16d55d100162fde4602e17ac89b0af9fdd5d4a`.

## 1.169.0 - M181 Lava bucket

Status: GO for official still-lava bucket pickup.

- Placed still lava `11:0` in a raised stone basin from the south wall.
- Empty bucket `325` picked up that source into lava bucket `327:1:0`.
- The empty basin and filled bucket survived save plus a fresh login.

The frozen M181 semantic SHA-256 is
`8389064523049de74163fc5f5c48e14d5e52eb750aee3eb297010fa2e87116d4`.

## 1.168.0 - M180 Fishing rod

Status: GO for official fishing-hook Packet23 type 90.

- Used fishing rod `346` in air from a raised stone platform.
- Two headless peers decoded the same Packet23 type `90` with thrower `0`.
- Pose was frozen at `138:2512:144`; catch RNG is not hashed.

The frozen M180 semantic SHA-256 is
`9eafaf3ce4f443aa63f94304c269f66ab3a4257921e9d595d17a208ef9c8554a`.

## 1.167.0 - M179 Wheat

Status: GO for official wheat planting on hydrated farmland.

- Wooden hoe `290` tilled dirt into farmland; still water hydrated a plot to `60:7`.
- Seeds `295` planted wheat `59:0` on that moist cell.
- The crop remained `59:0` after a live hold plus save and a fresh login.

The frozen M179 semantic SHA-256 is
`00d861629497b91621c26cc02b6ec8d56763ad9b4f365028fd10188e36694be8`.

## 1.166.0 - M178 Jukebox

Status: GO for official jukebox disc insert.

- Placed jukebox item `84` on a raised stone support as `84:0`.
- Used gold disc `2256` on that cell and decoded Packet61 effect `1005` data `2256`.
- The selected slot emptied and metadata `84:1` survived save plus a fresh login.

The frozen M178 semantic SHA-256 is
`97de107318c1552893e50f28f3483ac127f615e7ae8b5018e70e651c21886a86`.

## 1.165.0 - M177 Painting

Status: GO for official Packet25 painting spawn.

- Placed painting item `321` on a raised 2x2 stone wall.
- Two headless peers decoded the same Packet25 identity, pose and direction.
- Art title is observed but not hashed, because it can differ across JVMs.

The frozen M177 semantic SHA-256 is
`05ecb02dc2be9a42ab00eeae2c8c1eaf34609b0fe89c3c60aae4774b5e0e90d4`.

## 1.164.0 - M176 Sign

Status: GO for official standing-sign Packet130 text.

- Placed sign item `323` as standing sign `63:4`.
- Packet130 wrote `World` / `line` / `M176` / `ok`.
- A fresh login read the same four lines from inbound Packet130.

The frozen M176 semantic SHA-256 is
`02572936a90d996c81d67465e5507ff4f5ecb33262d10fd9cdb0e2cbe28489ff`.

## 1.163.0 - M175 Torch

Status: GO for official floor-torch placement.

- Placed torch item `50` on a raised stone support as `50:5`.
- That floor metadata survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M175 semantic SHA-256 is
`1b9a95028d397daf378283e42e4657f27df1e1e761003ef151f0fdd1790c3c3d`.

## 1.162.0 - M174 Ladder

Status: GO for official east-facing ladder placement.

- Placed ladder item `65` on an east stone face as `65:5`.
- That facing survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M174 semantic SHA-256 is
`901e76a85d36008f4429e6863549902a4c2b49485980fbfb65a9568725bb491e`.

## 1.161.0 - M173 Fence

Status: GO for official adjacent fence placement.

- Placed two fence items `85` as `85:0` on a raised stone support.
- Both cells survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M173 semantic SHA-256 is
`87da22ea0cb364e70239d8989a677f835f86ebbe9768dee6955f04ee4be1f74e`.

## 1.160.0 - M172 Wooden Pressure Plate

Status: GO for official wooden pressure-plate press and release.

- Placed wooden plate item `72` as `72:0` on a raised stone support.
- Walking onto the cell powered `72:1`; walking off restored `72:0`.
- The unpowered plate survived save plus a fresh login.

The frozen M172 semantic SHA-256 is
`ffcac8ad53202102f7e7ff5179823d53d8ecd116c879faba5a3c1ccf9bcd94c1`.

## 1.159.0 - M171 Pumpkin

Status: GO for official pumpkin placement facing.

- Placed pumpkin item `86` as `86:1` with look yaw `-90`.
- That exact metadata survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M171 semantic SHA-256 is
`239e38d65add8b63f1afa9dba90e1832f5b70b91bf6b6e5fee0df381e48c12e0`.

## 1.158.0 - M170 Repeater

Status: GO for official 1-tick repeater pulse.

- Placed repeater item `356` as unpowered `93:3` facing west.
- An input-side lever powered it to `94:3` after the 1-tick delay.
- Lever off restored `93:3`, which survived save plus a fresh login.

The frozen M170 semantic SHA-256 is
`6c15d889fbdd2c03553d0456cd4206acca9913147855898da285810b5cffe59b`.

## 1.157.0 - M169 Egg Throw

Status: GO for official Packet23 egg spawn.

- Used egg item `344` through Packet15 direction 255 from a raised platform.
- Two headless peers decoded the same Packet23 type `62` with thrower 0.
- Repeated the complete fixture in two official server JVMs.

The frozen M169 semantic SHA-256 is
`928748cb1070c773bf7991f2ccaa4384b51c7910f12d20cb86102e337da19585`.

## 1.156.0 - M168 Water Bucket

Status: GO for official still-water pickup into a bucket.

- Placed still water `9:0` in a raised stone basin.
- Empty bucket `325` plus Packet15 emptied the cell to air and filled `326:1:0`.
- The empty basin and water bucket survived save plus a fresh login.

The frozen M168 semantic SHA-256 is
`4ce39f3401e15de5c720a314091f69acf985c459785d211d52f84f4af9e47a7d`.

## 1.155.0 - M167 Cactus

Status: GO for official cactus placement on sand.

- Placed sand `12` then cactus item `81` as block `81:0`.
- The cactus remained after a 40-tick live hold and after save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M167 semantic SHA-256 is
`9a210a58a09a40ac501c31bf8262bee7846ea1240c7dc0654766374ba627ef30`.

## 1.154.0 - M166 Note Block

Status: GO for official note-block Packet54 click.

- Placed note block `25:0` on a raised stone support.
- Empty-hand activation emitted Packet54 instrument `1` pitch `1`.
- Block metadata stayed `25:0` and survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M166 semantic SHA-256 is
`ef6696d8923a3640502fdd0b5ff70c4945e9d080f7740b484b82d02f2e719228`.

## 1.153.0 - M165 Stone Button

Status: GO for official stone-button pulse and automatic depower.

- Placed button item `77` on an east stone face as `77:1`.
- Empty-hand activation powered to `77:9`.
- After the official 20-tick delay the button returned to `77:1`.
- The unpowered button survived save plus a fresh login.

The frozen M165 semantic SHA-256 is
`293f5014cd6d64c96de7f120544a33fdc68c2ac3843deba132c1d00ee8e00300`.

## 1.152.0 - M164 Pressure Plate

Status: GO for official stone pressure-plate press and release.

- Placed plate item `70` as `70:0` on a raised stone support.
- Walking onto the cell powered `70:1`; walking off restored `70:0`.
- The unpowered plate survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M164 semantic SHA-256 is
`ab14f3bebb0157e814af07dd4950065b472c5d5b99f25736c02b57fd08b1f754`.

## 1.151.0 - M163 Trapdoor

Status: GO for official trapdoor open and close.

- Placed trapdoor item `96` on an east stone face as `96:3`.
- Empty-hand activation opened to `96:7` and closed back to `96:3`.
- The closed trapdoor survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M163 semantic SHA-256 is
`a93d386ba46f2b1dd44b2e91c9fc6c758d267a1b17cda83414f994a1d7d9d1a8`.

## 1.150.0 - M162 Wooden Door

Status: GO for official wooden-door open and close.

- Placed door item `324` as lower `64:0` and upper `64:8`.
- Empty-hand activation opened to `64:4` / `64:12` and closed back.
- The closed pair survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M162 semantic SHA-256 is
`d14b3dc599ec9ecd4d0f39074ae46e401c37bd078d558f4b0dd0b477a3f1bfea`.

## 1.149.0 - M161 Snowball

Status: GO for one official Packet23 snowball spawn.

- Used snowball item `332` through Packet15 direction 255 from a raised platform.
- Two headless peers decoded the same Packet23 type `61` with thrower 0.
- Repeated the complete fixture in two official server JVMs.

The frozen M161 semantic SHA-256 is
`1865bda95354d11a5a95ac4eaaf3fb8ad521e4b6195e6c651715e6955e7a149d`.

## 1.148.0 - M160 Cake Eat

Status: GO for one official BlockCake bite.

- Placed cake item `354` as block `92:0` on a raised stone support.
- Empty-hand Packet15 bit the cake to `92:1` and healed Packet8 `17→20`.
- The bitten cake and full health survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M160 semantic SHA-256 is
`ec442ce01a43be294030f3c3fb48319afc75e6eb99291a4e24cab8a54f3d8074`.

## 1.147.0 - M159 Sugar Cane

Status: GO for official water-adjacent sugar-cane growth.

- Planted cane item `338` as reed `83` on dirt beside still water `9`.
- Official random ticks grew the cane to height 2 or more.
- The grown cane survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M159 semantic SHA-256 is
`70a49f193de25db52e447675752317f7e567b9817943e16a27eb25a669555d8f`.

## 1.146.0 - M158 Bed

Status: GO for official bed placement, daytime refusal, and SMP occupy/skip.

- Placed bed item `355` as foot `26:0` and head `26:8`.
- Daytime empty-hand use emitted `You can only sleep at night`.
- After `time set 18000`, Packet17 occupied the head `26:12` and SMP skipped to morning.
- Repeated the complete fixture in two official server JVMs.

The frozen M158 semantic SHA-256 is
`ab95c0893977d3774ddf9672b77063db206c52479e9645e917f6f0d42d49f2f0`.

## 1.145.0 - M157 Bow Arrow

Status: GO for one official Packet23 arrow spawn.

- Used bow `261` with arrow `262` through Packet15 direction 255.
- Two headless peers decoded the same Packet23 type `60` with the actor thrower.
- Repeated the complete fixture in two official server JVMs.

The frozen M157 semantic SHA-256 is
`abf0244450acb0b727df9080a6ca53849fcd0ca4ce62de83a13b815d04c8f917`.

## 1.144.0 - M156 Farmland Hydration

Status: GO for official hoe-to-farmland moisture.

- Wooden hoe Packet15 tilled dirt `3` into farmland `60`.
- Adjacent still water `9` hydrated at least one of four plots to `60:7`.
- The moist farmland survived save plus a fresh login.
- Repeated the complete fixture in two official server JVMs.

The frozen M156 semantic SHA-256 is
`fec7ee0f7082dd84e4b7dfdfb08bfecf7258e0369cad355481d6c673a7bebb3f`.

## 1.143.0 - M155 Minecart Spawn

Status: GO for one official Packet23 minecart spawn.

- Placed rail `66` on a raised stone column.
- Used minecart item `328` on that rail.
- Two headless peers decoded the same Packet23 type `10` at `144:2331:144`.
- Repeated the complete fixture in two official server JVMs.

The frozen M155 semantic SHA-256 is
`8bbf2ce26b50b36cdb15763b126864882c8e138b89113c1fe6dcd75988703fab`.

## 1.142.0 - M154 Boat Spawn

Status: GO for one official Packet23 boat spawn.

- Used boat item `333` through Packet15 direction 255 in a natural still-water cell.
- Two headless peers decoded the same Packet23 type `1` identity and pose `144:1993:144`.
- Public API owns object identity: `useSelectedItemInAir` plus `awaitObjectSpawn(type)`.
- Repeated the complete fixture in two official server JVMs.

The frozen M154 semantic SHA-256 is
`5da646e53a2e386476060c80fb7c8bce2d187f93133cb6adb76ac439e48439a6`.

## 1.141.0 - M153 Dispenser Eject

Status: GO for one official dispenser item eject.

- Opened Packet100 Trap window (type 3, 9+36 slots).
- Loaded cobblestone via accepted Packet102.
- Powered a west-facing dispenser `23:4` with a side lever.
- Observed Packet21 cobblestone eject and an empty dispenser slot.

The frozen M153 semantic SHA-256 is
`e9ac098cef87b90c28a3fdc264de812fb712489a228157a47e92fa23c958d3ec`.

## 1.140.0 - M152 Fire Wool Consumption

Status: GO for official wool consumption beside a netherrack flame.

- Ignited netherrack fire `51` with flint and steel.
- Placed wool `35` face-adjacent to that fire.
- After a bounded 1200-tick wait, a fresh login proved the wool cell consumed.
- Netherrack fire remained. Delay and air-versus-fire remainder are not hashed.

The frozen M152 semantic SHA-256 is
`76938b3c1a673ae193cb53be581ebf52977feb975f5edc7a614a544267681e46`.

## 1.139.0 - M151 Netherrack Fire

Status: GO for one official netherrack flame that survives a live hold and restart.

- Ignited air above netherrack `87:0` with flint and steel Packet15.
- Held fire `51:0` for 40 ticks on the live cache.
- Reloaded the same netherrack/fire pair after a clean save.
- Repeated the complete fixture in two official server JVMs.

The frozen M151 semantic SHA-256 is
`26bb6ad826b35c24a64688c8cc4ded9c503948812eb0c8b6007301c10f10f355`.

## 1.138.0 - M150 Pig Pork Drop

Status: GO for one official two-peer porkchop drop after adapter-owned death.

- Added adapter-owned observed movement, Packet7 attack and death waits.
- Routed Packet38/29 through one shared entity-event reader.
- Required a prior horizontal AI transition and at least one Packet21 pork `319`.
- Repeated the complete fixture in two official server JVMs.

The frozen M150 semantic SHA-256 is
`90cf54607ffd52b403765121c14d821e80e9996702f158c29efe63aee15b0d33`.

## 1.137.0 - M149 Pig Death

Status: GO for one official two-peer pig death transition.

- Added immutable Packet38/Packet29 `RemoteMobDeath` evidence.
- Added Packet7 diamond-sword mob attack on the existing animal-enabled profile.
- Correlated one spawned pig's death exactly across two peers.
- Repeated the complete death fixture in two official server JVMs.

The frozen M149 semantic SHA-256 is
`c189244beb58382402de4313f9d6be75c90f398e404a7df2ebbbdfa8b34c5048`.

## 1.136.0 - M148 Pig AI Movement

Status: GO for one official two-peer pig movement transition.

- Added immutable fixed-point Packet31/33/34 mob movement evidence.
- Added an opt-in animal-enabled official-server profile without changing the
  existing default profile.
- Correlated one spawned pig's horizontal movement exactly across two peers.
- Repeated the complete AI fixture in two official server JVMs.

The frozen M148 semantic SHA-256 is
`c265a9aa7d1e6254b11458774346f05613c82569948443803f4742740e933397`.

## 1.135.0 - M147 Piston Push Limit

Status: GO for the exact official twelve-versus-thirteen push boundary.

- Built matched vertical chains with alternating stone and cobblestone.
- Proved twelve blocks move through thirteen exact chain transitions.
- Proved thirteen blocks leave the piston and every payload cell invariant.
- Repeated both arms across four fresh official server JVMs.

The frozen M147 semantic SHA-256 is
`6fd354f14bc191c11fd670b0d58e6aa0b86072feec3bb2322261cef951ca1a54`.

## 1.134.0 - M146 Obsidian Piston Rejection

Status: GO for one official immovable obsidian payload.

- Built a powered normal piston against exact obsidian `49:0` and air.
- Proved piston, payload and destination remain invariant after activation.
- Required the one-cell raised digest containing only the lever transition.
- Repeated the complete fixture in two official server JVMs.

The frozen M146 semantic SHA-256 is
`5deacfe1aa98b05c6667cd13215354e232659bd057f50e1340640017dface768`.

## 1.133.0 - M145 Two-Block Piston

Status: GO for one official two-block piston chain.

- Built a normal piston with distinct stone/cobblestone payload cells.
- Observed all three exact payload transitions without identity ambiguity.
- Required the five-cell raised digest and fresh-session final states.
- Repeated the complete fixture in two official server JVMs.

The frozen M145 semantic SHA-256 is
`b086d950c86277e5c21762909ed03f11e3a5bd753aa2b5b0aa898edaf9adb88f`.

## 1.132.0 - M144 Sticky Piston Pull

Status: GO for one official sticky-piston pullback.

- Rebuilt M143's geometry with sticky piston `29` and independently reloaded
  its exact extended state.
- Deactivated the lever and observed head `34:12→stone 1:0` plus displaced
  stone `1:0→air`.
- Froze the four exact raised transitions that distinguish sticky pullback
  from normal-piston retention.
- Repeated the three-session lifecycle in two official server JVMs.

The frozen M144 semantic SHA-256 is
`a56517b95b971f64b951329f03267a3c65259a557dc00925e24e3f9235fe377a`.

## 1.131.0 - M143 Piston Retraction

Status: GO for one official normal-piston retraction.

- Rebuilt and extended the exact M142 fixture, then qualified it through a
  clean save and fresh client before treatment.
- Deactivated the lever and observed base `33:12→33:4` plus head `34:4→air`.
- Proved the non-sticky invariant that the displaced stone remains in its new
  cell while exactly three raised fixture states change.
- Repeated the complete three-session lifecycle in two official server JVMs.

The frozen M143 semantic SHA-256 is
`ed36c9824aa5c765b651fa5a53fa268e5427568f47fabaeb082ec26f7639e2e1`.

## 1.130.0 - M142 Piston Extension

Status: GO for one official lever-powered piston displacement.

- Built a west-facing normal piston with one stone in front and a side lever.
- Observed exact base `33:4→33:12`, stone-to-head `1:0→34:4`, and
  air-to-displaced-stone `0:0→1:0` transitions.
- Scoped the immutable delta to the raised fixture so generated water beneath
  the artificial tower is not falsely attributed.
- Reproduced the exact four-cell delta and fresh-session persisted states in
  two official server JVMs.

The frozen M142 semantic SHA-256 is
`48c199a75f4cb6d77ffd1cfec3081c5fa9880915553d5b7e913ddc7cb6a38a20`.

## 1.129.0 - M141 Pig Spawner Observation

Status: GO for one official living-entity creation and Packet24 observation.

- Added immutable `RemoteMobSpawn` and cumulative `MobObservationSession`.
- Added a bounded Packet24 queue and strict protocol-14 metadata decoder.
- Built a raised grass platform whose official default spawner creates pig
  type `90` inside the documented random volume.
- Correlated the identical positive entity identity and metadata across two
  simultaneous clients in two fresh official server JVMs.

The frozen M141 semantic SHA-256 is
`a148241c4e0282a64cf461ef362991e001cc17b1c7b06bd12e3f7b5b555fd522`.

## 1.128.0 - M140 Bonemeal Tree Growth

Status: GO for one official player-triggered vegetation generation boundary.

- Built a raised dry dirt fixture with exact oak sapling `6:0`.
- Applied bonemeal `351:15` through Packet15 and observed root log `17:0`.
- Required a persisted bounded-positive trunk and canopy while leaving exact
  randomized tree geometry server-authoritative.
- Reproduced the normalized root and structural evidence in two fresh official
  server JVMs.

The frozen M140 semantic SHA-256 is
`d5bca5667d5f93503d8c2226bf52d4e49d9395d51c2e2da675497b7d6a57d896`.

## 1.127.0 - M139 Water-Lava Reaction

Status: GO for one bounded official fluid-material reaction.

- Built a raised stone-confined basin with still lava `11:0` beside exact air.
- Placed still water `9:0` and observed vanilla neighbor processing convert the
  adjacent lava source to obsidian `49:0`.
- Scoped the causal hash to the two declared cells and reproduced both exact
  deltas in two fresh official server JVMs.
- Proved the water and obsidian states again after clean save and fresh login.

The frozen M139 semantic SHA-256 is
`1ba936e8c311e4af488c393c17f5f68031f6fbb2c7a8b4ae2831985900fcd326`.

## 1.126.0 - M138 Horizontal Lava

Status: GO for one bounded official scheduled lava transition.

- Built a raised two-cell stone trench with still source `11:0` and dirt gate.
- Removed the gate through Packet14 and observed exact Packet53 air.
- Waited through the slower vanilla schedule and observed target `11:2` while
  retaining source `11:0`.
- Scoped the causal hash to source/target so unrelated random ticks are not
  falsely attributed, then proved both states through a fresh client.

The frozen M138 semantic SHA-256 is
`f1d5832ac76c05b0cc786b294c8f29126f9d0a668c6326ca2ecae17b2824a760`.

## 1.125.0 - M137 TNT Explosion

Status: GO for one official isolated TNT blast and Packet60 observation.

- Added immutable `RemoteExplosion` evidence and `ExplosionSession`.
- Decoded the exact protocol-14 Packet60 layout, including its lack of later
  motion fields, and applied listed destroyed cells to the remote cache.
- Ignited TNT `46` with flint and steel, observed strength `4` and required the
  randomized blast list to contain the constructed stone support.
- Proved live support/TNT removal and the same air states after save and a
  fresh client login.

The frozen M137 semantic SHA-256 is
`bb96106b407266a1f02f9e9e8097e71f5d11de9337293e8cf063277cd00f07ed`.

## 1.124.0 - M136 Nether Death Respawn

Status: GO for one official Nether-death to Overworld-respawn lifecycle.

- Generalized `RemoteRespawn` evidence to retain source and destination
  dimensions while preserving its same-dimension constructor.
- Froze the production Packet9 Nether request as `09-ff`.
- Proved a skyless netherrack source view, signed nonpositive death health and
  authoritative Packet9 `-1→0` plus Packet8 health `20`.
- Proved dimension-change cache invalidation, Overworld-only replacement
  chunks and persisted `dimension:health = 0:20`.

The frozen M136 semantic SHA-256 is
`48c243301cfa00388490bde784ac80eb7597256aa539b83f1777b841d77148a1`.

## 1.123.0 - M135 Player Respawn

Status: GO for one official void-death and same-dimension respawn lifecycle.

- Added typed live health observation and a bounded `RespawnSession` boundary.
- Froze Packet9 requests for signed dimensions `0` and `-1` through the
  production encoder.
- Accepted vanilla's signed nonpositive overkill health without rewriting the
  packet, then required a fresh same-dimension Packet9 epoch and health `20`.
- Followed the server-selected corrected spawn into a decoded lit chunk and
  proved empty inventory plus persisted health after clean disconnect.

The frozen M135 semantic SHA-256 is
`22275e37f5b927fb38ddbe53bfb3869f752fa11afe00efc1e57d41edca84f81a`.

## 1.122.0 - M134 Nether Portal Roundtrip

Status: GO for one complete official Overworld-Nether-Overworld journey.

- Discovered the generated Nether portal from its decoded six-cell plane.
- Left the portal for a bounded 220-tick cooldown and re-entered it.
- Observed Packet9 `-1→0` and a second old-dimension cache invalidation.
- Proved a six-portal, fourteen-obsidian Overworld structure after return and
  persisted the player in dimension `0`; source reuse remains dynamic.

The frozen M134 semantic SHA-256 is
`c2f903638b1e364b9781c247e61c22c77a28a036212dbe444db5c62498e2a74b`.

## 1.121.0 - M133 Nether Portal Traversal

Status: GO for one official Overworld-to-Nether portal journey.

- Entered the active M132 portal for a bounded official residence interval.
- Observed Packet9 change the typed live session from dimension `0` to `-1`.
- Decoded the destination Nether chunk after old-world cache invalidation.
- Proved the server-generated 14-obsidian, six-portal counterpart and persisted
  the traversed player's Nether dimension.
- Kept the vanilla portal search's varying exact destination coordinate dynamic.

The frozen M133 semantic SHA-256 is
`5c8ac40f2065949243c4a0e77c0ae9f5757aa4d89247915f6878de01cb72ed5d`.

## 1.120.0 - M132 Nether Portal Activation

Status: GO for an official server-authored obsidian portal activation.

- Built a complete `4x5` frame using fourteen accepted Packet15 placements.
- Proved six empty interior cells before a flint-and-steel interaction.
- Observed exactly six portal block `90:0` transitions and retained the full
  frame and active portal through a fresh client session.
- Scoped the causal hash to the frame interior rather than unrelated scheduled
  world changes.

The frozen M132 semantic SHA-256 is
`033c56bdb9ddf8abbd27735158a33d88a6a07e85cb5294a09bde41e7015d6518`.

## 1.119.0 - M131 Dual-Dimension Session

Status: GO for simultaneous typed Overworld and Nether protocol sessions.

- Added the cumulative public `DimensionSession` boundary.
- Preserved Packet1's signed dimension byte and bounded exact-dimension waits.
- Added Packet9 change handling that clears old-dimension chunks while leaving
  redundant same-dimension respawns intact.
- Qualified concurrent dimensions `0` and `-1` with distinct decoded terrain.

The frozen M131 semantic SHA-256 is
`4fbbe9be7e3cd6ab8fbfddd920b11392711702505cfd14044e93128570b457cd`.

## 1.118.1 - M130 Nether Oracle Hardening

Status: GO after separating stable Nether structure from scheduled fluid and
decoration changes.

- Retained exact positional netherrack and bedrock evidence.
- Normalized lava 10/11 and mushroom decoration 39/40 after fresh runs exposed
  legitimate pre-capture variation.
- Refroze the honest structural signature without broadening M130's claim.

The hardened M130 semantic SHA-256 is
`d04ef062cdda13bb2209d8f6651f0559495d9a9f63f946f460b0e8610c41c4a8`.

## 1.118.0 - M130 Nether Login

Status: GO for one official dimension-seeded Nether login and chunk decode.

- Added an opt-in `allow-nether=true` server profile without changing defaults.
- Added exact Overworld/Nether player NBT seeding.
- Decoded and structurally hashed the first official Nether chunk and verified
  saved `Dimension=-1` after clean logout; variable lava flow and mushroom
  decoration are explicitly excluded.

The frozen M130 semantic SHA-256 is
`d04ef062cdda13bb2209d8f6651f0559495d9a9f63f946f460b0e8610c41c4a8`.

## 1.117.0 - M129 Cross-Chunk Iron Door Recovery

Status: GO for exact recovery of the cross-chunk multiblock consumer.

- Qualified the open door and powered lever through a fresh client.
- Observed exact lever `9→1`, lower-door `4→0` and upper-door `12→8` metadata.
- Proved inverse `2 + 1` deltas and zero residual states against baseline.

The frozen M129 semantic SHA-256 is
`5a5478fd4aea68c69ed892984bc98353e208065f354857a047bac7f38c00cfac`.

## 1.116.0 - M128 Cross-Chunk Iron Door

Status: GO for one cross-chunk redstone source driving a multiblock consumer.

- Placed the lever at global x=16 and the iron door at global x=15.
- Observed exact lever `1→9`, lower-door `0→4` and upper-door `8→12` metadata.
- Froze two state deltas in the door chunk and one in the lever chunk.

The frozen M128 semantic SHA-256 is
`96c3cc2f75a5a3864e6be8991639b6e92c5d89a71ec0a88eb788dfca0c05a3c4`.

## 1.115.0 - M127 Cross-Chunk Redstone Recovery

Status: GO for exact recovery of one redstone signal across a chunk seam.

- Qualified the powered `69:9` lever and `55:15` wire through a fresh client.
- Deactivated the lever once and observed exact `9→1` and `15→0` transitions.
- Proved one inverse delta per chunk and zero residual states against baseline.

The frozen M127 semantic SHA-256 is
`269f3a7043dc7c483f160233c36890ef075faf03e36300801aa5779f06b05aa2`.

## 1.114.0 - M126 Cross-Chunk Redstone

Status: GO for one lever-to-wire signal across a chunk seam.

- Placed lever `(16,64,3)` and wire `(15,65,3)` in adjacent chunks.
- Proved one Packet15 activation changes lever metadata `1` to `9` and wire
  power `0` to `15`.
- Froze exactly one state delta in each complete post-activation chunk snapshot.

The frozen M126 semantic SHA-256 is
`1464edc1c01b62563d3608f6b60b9ba6ee30470dbf16e2111ac2c2cd59e880e5`.

## 1.113.0 - M125 Cross-Chunk Water

Status: GO for one bounded fluid transition across a chunk seam.

- Built a stone trench with source `9:0` at global x=15 and a dirt gate at x=16.
- Opened the sole destination through Packet14 and observed exact water `9:1`.
- Froze an empty source-chunk delta and exact one-state neighboring-chunk delta.

The frozen M125 semantic SHA-256 is
`c876ddf9f8686e16db848fb38977ff02ea8eb97dea05e21b0837be68f83a6217`.

## 1.112.0 - M124 Cross-Chunk Light Recovery

Status: GO for exact recovery of two block-light planes after source removal.

- Captured M123's lit source and neighbor chunks with an independent client.
- Removed the edge glowstone through Packet14 and observed ocean water restore
  exact source state `9:0`.
- Proved all 55/19 light changes reverse and both baseline-to-final delta sets
  are empty.

The frozen M124 semantic SHA-256 is
`60903e4d40e5297e01412eb69996ce5f3e2b641f1898d67f376ff357d016dbce`.

## 1.111.0 - M123 Cross-Chunk Lighting

Status: GO for one causal vanilla block-light transition across a chunk seam.

- Replaced exact edge water `9:0` at global `x=15` with glowstone `89:0`.
- Proved source light `0→15` and neighboring-chunk water light `0→12` through
  fresh complete Packet51 snapshots.
- Froze 55 source-chunk and 19 neighboring-chunk increases while both skylight
  planes remained unchanged.

The frozen M123 semantic SHA-256 is
`7f93c32c82a360dcdc5c546f69838e8fcbc8a221bf8ad2961bd532876608365a`.

## 1.110.0 - M122 Fixed-Seed Region Lighting

Status: GO for exact block-light and sky-light planes across nine chunks.

- Reused M121's 200-heartbeat, save, restart and fresh Packet51 lifecycle.
- Froze 294,912 exact nibbles and sixteen histogram bins independently for
  each vanilla light plane across chunks `(-1,-1)` through `(1,1)`.
- Applied no normalization and kept causal lighting and arbitrary regions
  outside the claim.

The frozen M122 semantic SHA-256 is
`55f946b28a62caf43a7b02b027f13747f5662e315fbf0c8e70f9cca77a189192`.

## 1.109.0 - M121 Fixed-Seed Region

Status: GO for one exact nine-chunk vanilla surface and solid-seam census.

- Loaded chunks `(-1,-1)` through `(1,1)` in two fresh fixed-seed worlds and
  repeated the census after 200 heartbeats, clean save and official restart.
- Froze the identical 128,529-block aggregate, exact 2,304-column surface and
  solid occupancy across all internal chunk seams.
- Preserved divergent interior masks, raw IDs and metadata as diagnostics
  instead of overstating official scheduled/random-tick determinism.

The frozen M121 semantic SHA-256 is
`c2a08f5e7e5ec2b6767afbc4b26409d586f2fd4ca296d199d823abe8b2b73d4f`.

## 1.108.0 - M120 Horizontal Water

Status: GO for one bounded server-authored horizontal fluid transition.

- Built a stone trench with exact source water `9:0` and one dirt-gated exit.
- Used fresh Packet51 baselines before and after treatment to exclude stale
  incremental ocean-water normalization from the causal delta.
- Proved Packet14 opening produces target `9:1` and exactly one full-chunk
  state change while keeping generic fluids and natural bucket use out of scope.

The frozen M120 semantic SHA-256 is
`c0bbf83eadc6fd56c3697b50ed2d653aebc2fd9e132467354a9bcae89a6daa29`.

## 1.107.0 - M119 Falling Sand

Status: GO for one server-authored block-gravity transition.

- Stabilized sand `12:0` above a stone support and removed that support through
  the qualified Packet14 boundary.
- Required transient lower air, then exact lower sand and upper air through
  live Packet53 state and a fresh Packet51.
- Froze exactly two full-chunk changes while keeping generic gravity, falling
  entities, long falls, gravel and collision behavior outside scope.

The frozen M119 semantic SHA-256 is
`ac00ec1900fdfc0489c6e7d4e9621c916411505d522df3c1fc9f3c53a78eb656`.

## 1.106.0 - M118 Redstone Iron Door

Status: GO for one server-authored powered-consumer transition.

- Used official iron-door item 330 to create exact block-71 bottom/top states
  `0/8` above the stabilized column.
- Activated the adjacent side lever and proved `69:1 -> 69:9`, door bottom
  `0 -> 4`, and door top `8 -> 12` through Packet53 and fresh Packet51.
- Froze exactly three full-chunk state changes while keeping generic consumers,
  closing, topology, collision and tick-exact ordering outside scope.

The frozen M118 semantic SHA-256 is
`e2000f240f0dce5e5fe233611cca6053e50b31c57113fd564387a00f527d7573`.

## 1.105.0 - M117 Redstone Wire Depower

Status: GO for one server-authored lever-to-wire recovery result.

- Rebuilt M116's exact official lever/wire fixture and required the powered
  `69:9` / `55:15` precondition before treatment.
- Toggled the same lever off and proved Packet53 plus a fresh Packet51 expose
  `69:1` / `55:0` after ten bounded signal ticks.
- Froze exactly the two reverse full-chunk state deltas while keeping generic
  topology, attenuation, consumers and tick-exact ordering outside scope.

The frozen M117 semantic SHA-256 is
`87c06977c34465cb580ba9a857102c62e6953ede7cfe339c2730fc9673a699fe`.

## 1.104.0 - M116 Redstone Wire Power

Status: GO for one server-authored lever-to-wire propagation result.

- Added selected held-item-on-block use across the positive protocol item-ID
  range while retaining authoritative inventory and cursor/window guards.
- Used redstone dust 331 to create official wire `55:0` on M115's stabilized
  fixture.
- Proved lever activation changes `69:1 -> 69:9` and propagates wire power
  `55:0 -> 55:15` through Packet53 and a fresh Packet51.
- Froze exactly two full-chunk state deltas and kept generic redstone networks,
  attenuation, depowering, consumers and timing outside qualification.

The frozen M116 semantic SHA-256 is
`973fb75a9541e4f8015d8133d7c99779e6c1ab8b6ef095120609e6a6fcab5587`.

## 1.103.0 - M115 Lever Activation

Status: GO for one server-authored redstone-component activation.

- Added a neutral empty-hand `activateBlock(position, face)` multiplayer
  boundary with personal-window/cursor/selected-hand preconditions.
- Built a deterministic ten-stone above-water side-lever fixture, fixed yaw and
  waited 200 ticks to exclude orientation and fluid drift.
- Proved Packet15 activation changes lever `69:1` to `69:9` through Packet53
  and a fresh Packet51, with exactly one full-chunk state delta.
- Kept redstone propagation, circuits, powered consumers and generic block
  interaction outside qualification.

The frozen M115 semantic SHA-256 is
`497b5d743a5693c925d69d71c02528cf2d16a63ad5c477980b916a0d2b45ae34`.

## 1.102.0 - M114 Causal Water Flow

Status: GO for one server-authored downward vanilla-water transition.

- Broke one dirt cell below naturally generated still water through the
  existing Packet14 begin/finish boundary and required Packet53 air first.
- Proved live and fresh-login observations settle the opened cell from air to
  water `9:8` after forty heartbeats.
- Froze one and only one full-chunk state delta across two fresh official
  worlds and four protocol sessions.
- Generalized local hurt tracking to accept valid ordered health decreases
  while preserving caller-supplied expected-health checks.
- Replaced M66's `/give`/drop/fall fixture with exact official-NBT inventories
  and bounded air-position heartbeats; its frozen combat evidence is unchanged.
- Kept generic fluids, lava/mixing, lateral flow, buckets, timing, rendering
  and cross-chunk behavior outside qualification.

The frozen M114 semantic SHA-256 is
`658a1cbfc4555fb57b3cef83375f655232f18b834afe547330fd96e64c8a5e3e`.

## 1.101.0 - M113 Causal Lighting

Status: GO for one server-authored vanilla light-source transition.

- Seeded one exact glowstone stack in official-format player NBT and placed it
  through the existing protocol-14 selected-slot/block-placement boundary.
- Required Packet53 acceptance, forty update heartbeats, clean disconnect/save
  and a fresh Packet51 light-plane observation.
- Froze exactly 68 increased block-light samples, maximum delta 15 and source
  level 15; sky light remained unchanged.
- Kept generic propagation/removal, cross-chunk light, rendering, other
  sources, alternate terrain and dimensions outside qualification.

The frozen M113 semantic SHA-256 is
`c54effdf42a0dcf7c37c7417e2a35d0abfdc85297b2b47398af1d4d86632c822`.

## 1.100.0 - M112 Fixed-Seed Lighting

Status: GO for deterministic vanilla block-light and sky-light snapshots.

- Decoded all 32,768 nibbles from each light plane in absolute chunk `(0,0)`
  across two fresh official worlds.
- Froze exact block-light and sky-light hashes plus complete 0–15 histograms.
- Added a minimal official-format player-NBT seed so fixed-coordinate world
  observations do not depend on Beta 1.7.3's variable spawn search.
- Kept source attribution, light updates, cross-chunk propagation, rendering,
  alternate seeds and dimensions outside qualification.

The frozen M112 semantic SHA-256 is
`f5180dc49e6d6117c501e903ab16b1015a071cedf027e2444168a40109dc0969`.

## 1.99.0 - M111 Fixed-Seed Terrain

Status: GO for deterministic vanilla terrain at one absolute chunk.

- Generated two fresh worlds with the unmodified official Beta 1.7.3 server
  and fixed seed `17320110707`.
- Decoded all 32,768 block IDs in absolute chunk `(0,0)` and proved identical
  full-volume and 256-column top-Y/ID/metadata surface digests.
- Kept the version's variable player spawn, lighting, biomes, other chunks,
  alternate seeds, dimensions and persistence outside qualification.

The frozen M111 semantic SHA-256 is
`1242a03c15a6e0c36adbefb6ca2b89b166ab1b57f5fb20cf6d3f402a0bec50b1`.

## 1.98.0 - M110 Cell Size Ceiling

Status: GO for the raw-thirty-three/explicit-thirty-two clamp comparison.

- Ran two balanced same-plan fresh-process pairs with minimum2,
  skip-individual false, pages enabled, unlimited cache/rebuild, and TTL100000.
- Proved raw thirty-three and raw thirty-two both exposed effective size32.
- Required every retained record in both arms to keep queue16/rendererCalls16,
  flush2, pageCalls1, direct0, cache1, M74 render/list0/0, rebuild0,
  immediate0, and eviction0.
- Kept generic clamping, configuration quality and all timing directions
  outside qualification.

The frozen M110 semantic SHA-256 is
`4061454ff65c9ef06366042094e79fc165c26e91d6f3af2fcd7f04638a180c0e`.

## 1.97.0 - M109 Cell Size Floor

Status: GO for the raw-zero/explicit-one clamp comparison.

- Ran two balanced same-plan fresh-process pairs with minimum2,
  skip-individual false, pages enabled, unlimited cache/rebuild, and TTL100000.
- Proved raw zero and raw one both exposed effective cell size one.
- Required every retained record in both arms to keep queue16/rendererCalls16,
  flush2, pageCalls0, direct16, cache0, M74 render/list16/16, rebuild0,
  immediate0, and eviction0.
- Kept generic clamping, configuration quality and all timing directions
  outside qualification.

The frozen M109 semantic SHA-256 is
`d5ba4fa589d791959dca34158989889ea9d5c29942b6bc44fca7a18bb800a69e`.

## 1.96.0 - M108 Paired Cell Size

Status: GO for the exact size-two/size-eight comparison.

- Ran two balanced same-plan fresh-process pairs with minimum2,
  skip-individual false, pages enabled, unlimited cache/rebuild, and TTL100000.
- Proved the fixed aligned plan produced pageCalls4/cache4 at size two and
  pageCalls1/cache1 at size eight in every retained record.
- Required both arms to keep queue16/rendererCalls16/flush2, direct0, rebuild0,
  M74 render/list0/0, immediate0, and eviction0.
- Kept memory cost, visual equivalence, better-size and all timing directions
  outside qualification.

The frozen M108 semantic SHA-256 is
`7bd2dd0f5f557a19c07eaf9d79978bfbac81aee3ad313df51ac504740b7c303d`.

## 1.95.0 - M107 Paired Skip Individual

Status: GO for the exact skip-true/skip-false comparison.

- Ran two balanced same-plan fresh-process pairs with minimum2, pages enabled,
  unlimited cache/rebuild budget, and TTL100000.
- Proved skip true used sixteen managed pre-dispatch enqueues and zero
  individual renderer calls in every retained record.
- Proved skip false used sixteen manual enqueues through sixteen individual
  renderer calls while retaining the same four-page structural state.
- Required both arms to keep pageCalls4, direct0, rebuild0, cache4, M74
  render/list0/0, immediate0, and eviction0.
- Kept visual equivalence and all timing directions outside qualification.

The frozen M107 semantic SHA-256 is
`913fff54f216f47e06d3886f94f4682b83c1d5bbf49648991c28926d71e8c6f3`.

## 1.94.0 - M106 Paired Minimum Instances

Status: GO for the exact minimum-two/minimum-five comparison.

- Ran two balanced same-plan fresh-process pairs with pages enabled, unlimited
  cache/rebuild budget, and TTL100000.
- Proved minimum2 retained four cached page calls with no direct fallback and
  M74 render/list counters0/0 in every retained record.
- Proved minimum5 retained two cached page calls plus four direct instances and
  M74 render/list counters4/4 in every retained record.
- Fixed only test-client yaw/pitch before readiness while preserving strict
  server-authored X/Y/Z validation, eliminating physical mouse drift.
- Kept all timing directions outside qualification.

The frozen M106 semantic SHA-256 is
`f3b298b76961b50be8e4695957f53c7ee1e735d394d0b26886e8c5164553adae`.

## 1.93.0 - M105 Paired Cache Capacity

Status: GO for the exact cache1/unlimited-capacity comparison.

- Ran two balanced same-plan fresh-process pairs with pages=true,
  rebuilds=-1, and TTL100000.
- Proved capacity1 rebuilt four pages, retained one, and advanced eviction by
  four in every retained record.
- Proved capacity-1 retained four warmed pages with rebuild0 and eviction0 in
  every retained record.
- Added and validated one vanilla stone support block outside the Aero fixture
  so the unchanged strict camera pose cannot race gravity in replayed plans.
- Kept memory cost and all timing directions outside qualification.

The frozen M105 semantic SHA-256 is
`35da2fabb47ef902a2cbd7b92dc976771d9a80179b76322cf1f26edade4e5898`.

## 1.92.0 - M104 Paired Pages Control

Status: GO for the exact balanced pages-enabled/pages-disabled comparison.

- Ran two balanced fresh-process pairs with the same plan and nonce per pair.
- Proved the enabled path had queue16/pageCalls4/rebuild4/cache1 and no
  immediate calls in every record.
- Proved the disabled path had immediateDirect16 and zero page state in every
  record, with aligned M74 render/list16/16.
- Hardened automatic plans to include the camera support column while keeping
  the strict full-pose readiness gate.
- Kept paired timing summaries dynamic and made no causal or relative-
  performance claim.

The frozen M104 semantic SHA-256 is
`a91f910fbbf2ced951e0a009e1db64924f8b8a33f34aeca4f8b0e6b6e2bc4df8`.

## 1.91.0 - M103 Pages-Disabled Immediate Direct

Status: GO for the exact explicit pages-disabled control.

- Froze literal pages=false with cache1, rebuild sentinel -1, and TTL100000.
- Added a primitive sidecar counter on the exact immediate `drawDirect`
  overload, avoiding the later empty-flush reset of public cell counters.
- Proved immediateDirect16 and M74 renderer/list16/16 in every one of
  4021/3673 records while queued/page/rebuild/cache/eviction stayed zero.
- Kept timing values dynamic and made no relative-performance claim.

The frozen M103 semantic SHA-256 is
`7ebb83eada0eccda5dbb38d2610d92b60abe893d2483212710eb463e0aa285c6`.

## 1.90.0 - M102 Unlimited-Rebuild Sentinel

Status: GO for literal negative-one unlimited rebuilding under cache1.

- Froze cache1, TTL100000, rebuild sentinel -1, and the four-page scene.
- Proved every retained record had pageCalls4, direct0, rebuild4, cache1, and
  cumulative capacity evictions advancing by four.
- Independently required M74 direct renderer/list counters 0/0 and all sixteen
  synchronized identities in 4724/4586 complete records.
- Kept descriptive timings dynamic and bounded the mechanism to literal -1,
  the exact fixture, and pinned Aero revision.

The frozen M102 semantic SHA-256 is
`852d41f2d1654fd1dc83d0b746fddb4c109d370573fd67b25290361ddaefa75b`.

## 1.89.0 - M101 Rebuild-Budget-Zero Direct Path

Status: GO for the exact zero-rebuild direct path under cache1.

- Froze cache1, TTL100000, rebuild budget0, and the existing four-page scene.
- Proved every retained record had pageCalls0, directInstances16, rebuild0,
  cache0, and zero cumulative capacity evictions.
- Independently required M74 renderer/list counters 16/16 and all sixteen
  synchronized identities in 4490/4758 complete records.
- Kept descriptive timings dynamic and bounded the mechanism to the exact
  fixture and pinned Aero revision.

The frozen M101 semantic SHA-256 is
`8e0d8ae9c249c8f2967e0ac534c0ee7b7e79ff6a04bd7b407c89dcd2f5e7b0cd`.

## 1.88.0 - M100 Rebuild-Budget-One Fallback

Status: GO for the exact alternating one-rebuild path under cache1.

- Froze cache1, TTL100000, rebuild budget1, and the existing four-page scene.
- Proved strict alternation between `pageCalls2/direct4` and
  `pageCalls1/direct10`, with aligned M74 renderer/list counters.
- Required rebuild1, cache1, and capacity-eviction delta one in every one of
  4771/4892 complete records; mode counts were 2386/2385 and 2446/2446.
- Kept descriptive timings dynamic and bounded the mechanism to the exact
  fixture and pinned page order.

The frozen M100 semantic SHA-256 is
`322cccb6a7643bf79357d81d1c8b3ecf2bc0c7bcad170993ebbb01fc7fa8d76b`.

## 1.87.0 - M99 Rebuild-Budget Fallback

Status: GO for the exact two-page rebuild budget under one-entry cache pressure.

- Froze cache1, TTL100000, rebuild budget2, and the existing four-page scene.
- Proved every retained record had pageCalls2, directInstances4, rebuild2,
  cache1, and capacity-eviction delta two.
- Independently required M74 direct renderer/list counters 4/4 and all sixteen
  synchronized identities in 5003/4223 complete records.
- Kept descriptive timings dynamic and limited the split to this exact sorted
  page topology and membership distribution.

The frozen M99 semantic SHA-256 is
`bc072d0104007b86828550033fb0aa3e84c179aa5caee84dcd22552c3c9a4ce7`.

## 1.86.0 - M98 Configured-Zero Protected Cache Floor

Status: GO for the protected one-page floor under literal max-cache zero.

- Froze `maxCachedPages=0` with TTL 100000 and the exact four-page fixture.
- Proved every retained record nevertheless had cache1, pageCalls4, direct0,
  rebuild4, and capacity-eviction delta four.
- Retained 4133/3991 complete records with zero rebuild3 records.
- Documented that protected-key eviction prevents zero from disabling paging
  or producing an empty cache in this path.

The frozen M98 semantic SHA-256 is
`0da3de05b8d5c493b974e04eaf1767e07f54b087f387badfdaf5dd48b6f1bb31`.

## 1.85.0 - M97 Page-Capacity-One Thrash

Status: GO for exact all-page rebuild/eviction behavior under a one-entry Aero
page cache.

- Froze capacity one with TTL 100000, rebuild budget eight, and the existing
  sixteen-cell/four-page fixture.
- Proved all retained records had cache1, pageCalls4, direct0, rebuild4, and
  cumulative capacity-eviction delta four.
- Retained 5067/4581 complete records with `rebuild3=0`, separating the result
  from M96's capacity-two tie modes.
- Reparsed every M74/M97 record and required clean pinned lifecycle/provenance.

The frozen M97 semantic SHA-256 is
`93c51ccdd98d0abd4e6da174f6ea76d8ca10ddb31cfed965117945473a39c551`.

## 1.84.0 - M96 Page-Capacity-Two Bounded Thrash

Status: GO for bounded rebuild/eviction behavior of the exact four-page scene
under a two-entry Aero page cache.

- Froze capacity two with TTL 100000, rebuild budget eight, and the existing
  sixteen-cell/four-page fixture.
- Accepted only rebuild counts three or four and required each record's
  cumulative eviction delta to equal its rebuild count exactly.
- Observed one 4980-record rebuild3 replica and one 4552-record rebuild4
  replica, exposing pinned JVM/hash tie behavior without fixing mode assignment.
- Preserved four page calls, zero direct fallback, two flushes, and complete
  M74/M96 artifact reconciliation throughout.

The frozen M96 semantic SHA-256 is
`96142417765b773152dc82aba8194765319c2c7bd987d513c5b8b8fd34b89acb`.

## 1.83.0 - M95 Page-Capacity Thrash

Status: GO for the exact four-page fixture under a three-entry Aero page cache.

- Froze cache capacity three, page TTL 100000, rebuild budget eight, and the
  existing fixed sixteen-cell/four-page scene.
- Proved every retained record had cache3, pageCalls4, direct0, rebuild2, and
  exactly two new capacity evictions.
- Bound cumulative evictions and renderer/enqueue/flush spans to aligned
  56-byte records and reparsed every complete M74/M95 record in two replicas.
- Observed 5565/5549 samples with eviction ranges 5..11133 and 5..11101;
  timing values remain descriptive.

The frozen M95 semantic SHA-256 is
`4792da7a14435f7c4abeb761e4b22021b7afe0dc617b33422afba4d087035fa5`.

## 1.82.0 - M94 Default-TTL Page Recovery

Status: GO for default-TTL expiration and reverse recovery of M93's exact
empty six-member page.

- Left both `aero.becell.pageTtlFrames` and `aero.perf.memory` unset, binding
  the pinned normal default of 600 frames.
- Proved one empty-page cache expiration: cached pages `4 -> 3`, expired
  counter `0 -> 1`, and zero max-cache evictions in two fresh replicas.
- Delayed restoration until 30 complete records after the expiry record;
  first member stayed direct/cache3 and second rebuilt/cache4.
- Bound expiry counters plus all twelve transitions to a 184-byte sidecar and
  reparsed every complete M74/M78 record.

The frozen M94 semantic SHA-256 is
`c2617f80713c9054acdf8ade17e4474a3a1ed275a2c092fc6d455363493acfcf`.

## 1.81.0 - M93 Full-Page Depletion Recovery

Status: GO for complete depletion and reverse recovery of one exact pinned
six-member Aero page under a fixed page TTL.

- Removed indices `1,2,3,5,6,7`, then restored `7,6,5,3,2,1` with exact
  ordinal, operation, index, coordinate, nonce, ACK, and state validation.
- Proved membership `16 -> 15 -> 14 -> 13 -> 12 -> 11 -> 10 -> 11 -> 12 ->
  13 -> 14 -> 15 -> 16` across two fresh same-plan replicas.
- Distinguished the batched two-through-six-member route, the direct
  one-member route, and the zero-member route, with symmetric restoration.
- Runtime-gated page TTL at 100000 frames and bound twelve transitions to a
  172-byte sidecar; default-TTL eviction remains a nonclaim.

The frozen M93 semantic SHA-256 is
`f0f506ffa69950d8d4030819a4c6c5ca3f190edcfd3f4ba29f3a4ef4129959ad`.

## 1.80.0 - M92 Third-Member Depletion Recovery

Status: GO for sequential three-cell depletion and reverse recovery inside one
exact pinned six-member Aero page.

- Removed indices one/two/three, then restored three/two/one with exact
  ordinal, operation, index, coordinate, nonce, ACK, and state validation.
- Proved membership `16 -> 15 -> 14 -> 13 -> 14 -> 15 -> 16`; every transition
  retained four page calls, zero direct fallback/render/list, and one rebuild.
- Preserved four cached pages and complete M74 state `0x1010/0xffff`.
- Bound all six transitions to a 100-byte sidecar and reparsed every matching
  M74/M78 record in two fresh same-plan replicas.

The frozen M92 semantic SHA-256 is
`a82e3eb16c9c12a3901e03775d53898a725914562f5bd971d7dc5d2444c75104`.

## 1.79.0 - M91 Larger-Page Depletion Recovery

Status: GO for sequential two-cell depletion and reverse recovery inside one
exact pinned six-member Aero page.

- Removed indices one and two, then restored two and one with exact ordinal,
  operation, index, coordinate, nonce, ACK, and restore-state validation.
- Proved membership `16 -> 15 -> 14 -> 15 -> 16`; every transition retained
  four page calls, zero direct fallback/render/list calls, and one rebuild.
- Preserved four cached pages and complete M74 state `0x1010/0xffff`.
- Bound all four transitions to a 76-byte sidecar and reparsed every matching
  M74/M78 record in two fresh same-plan replicas.

The frozen M91 semantic SHA-256 is
`5f019eb32c7f34b31ca907e9fdbec3b827254a08cdf0cbe11a91c703644b2f7e`.

## 1.78.0 - M90 Larger-Page Sibling Recovery

Status: GO for exact index-two removal/restoration in index one's natural
six-member page under the pinned Aero configuration.

- Targeted `(x,y+2,z)` and derived nonce `root*100+3`, with exact block,
  block-entity, phase, ACK, and restored-state validation.
- Proved membership `16 -> 15 -> 16` while page calls remained four, direct
  fallback/render/list calls remained zero, and both transitions rebuilt once.
- Preserved four cached pages and complete M74 state `0x1010/0xffff` throughout.
- Bound both request/event pairs to the 52-byte sidecar and reparsed every
  corresponding M74/M78 record.

The frozen M90 semantic SHA-256 is
`aac17bb2f371a10cf09b7350c228e000700ac36270dc6d3535e3de74a132a402`.

## 1.77.0 - M89 Sibling-Cell Membership Recovery

Status: GO for exact index-four removal/restoration in index zero's natural
two-member page under the pinned Aero configuration.

- Targeted `(x,y,z+1)` and derived nonce `root*100+5`, with exact block,
  block-entity, phase, ACK, and restored-state validation.
- Proved membership `16 -> 15 -> 16`, page calls `4 -> 3 -> 4`, direct fallback
  `0 -> 1 -> 0`, and one rebuild only at restoration in two fresh replicas.
- Preserved four cached pages and complete M74 state `0x1010/0xffff` throughout.
- Bound both request/event pairs to the 52-byte sidecar and reparsed every
  corresponding M74/M78 record.

The frozen M89 semantic SHA-256 is
`87fa014b6cd31a48c7cffa7f839d0b407ecf823d815a80f1a578afa00828c649`.

## 1.76.0 - M88 Reverse Two-Cell Membership Recovery

Status: GO for reverse-order generation-bound recovery over the two exact M87
cells in the same synchronized Aero fixture.

- Removed/restored index one before index zero while preserving the M87 seed,
  plan, nonce, camera, cache, protocol, spacing, and complete record window.
- Proved membership `16 -> 15 -> 16 -> 15 -> 16` with exact generation/index
  ACK and restore-state binding in two fresh replicas.
- Observed index one rebuild immediately even when first, while index zero used
  the direct fallback even when second; both restores rebuilt once.
- Bound four request/event/index triples to the 76-byte sidecar and reparsed
  every corresponding M74/M78 record.

The frozen M88 semantic SHA-256 is
`986d67c17068113e152c7cec8614bbc518629fff4c27619ec488da6c2548c079`.

## 1.75.0 - M87 Two-Cell Membership Recovery

Status: GO for sequential generation-bound recovery over two distinct cells in
one exact server-authored synchronized Aero fixture.

- Removed and restored cell indices zero and one in separate generations, with
  exact coordinate, derived-nonce, operation, generation, and index binding.
- Proved membership `16 -> 15 -> 16 -> 15 -> 16` while preserving the complete
  M74 state and identity mask in every retained record.
- Observed the first removal use the qualified direct fallback, while the second
  removal rebuilt immediately after the first recovery and did not fall back.
- Bound all four request/event/index triples to a 76-byte sidecar and reparsed
  the corresponding complete M74/M78 records in two fresh replicas.

The frozen M87 semantic SHA-256 is
`091dd5a68a9e7650ef91496f86cbc9dc5e82e006863d097a8e3c637402a103a4`.

## 1.74.0 - M86 Repeated Membership Recovery

Status: GO for two generation-bound remove/restore cycles over the same exact
server-authored synchronized Aero cell.

- Preserved M85's seed, plan, nonce, camera, cache, and recording window while
  adding generations one and two to requests, ACKs, and restore state.
- Proved membership `16 -> 15 -> 16 -> 15 -> 16`, the same three-page plus one
  fallback topology in both removed intervals, and one rebuild at each restore.
- Rejected duplicate, skipped, reordered, cross-generation, wrong-coordinate,
  and wrong-nonce protocol state.
- Bound four request/event pairs to a 60-byte sidecar and reparsed topology from
  the corresponding complete M74/M78 records in two fresh replicas.

The frozen M86 semantic SHA-256 is
`841b311c16d11cbbe669756fd0fc020c4371b650ad9c185d8ab717c7217abc44`.

## 1.73.0 - M85 Natural Membership Recovery

Status: GO for one exact natural remove-then-restore sequence over the same
server-authored synchronized Aero cell.

- Removed cell index zero after retained record 300, with strict block, BE,
  root nonce, derived nonce, server ACK, and client air-block validation.
- Restored the same cell after thirty retained records with a dedicated
  buffered state packet and exact block-entity/nonce convergence.
- Proved membership `16 -> 15 -> 16`, page calls `4 -> 3 -> 4`, direct fallback
  and render/list calls `0 -> 1 -> 0`, and one rebuild only at restoration.
- Bound both request/event pairs to a 52-byte sidecar and complete M74/M78
  records in two fresh same-plan replicas; timing remains descriptive.

The frozen M85 semantic SHA-256 is
`6afe38b10186f67d95eef5d1a1beca81bd168417d7d32d3579dfd654aae0445b`.

## 1.72.0 - M84 Four-Page Topology Contrast

Status: GO for a constant-three-member one-page versus three-page structural
transition over one fixed four-page-key scene.

- Aligned a 4x4 scene across Y/Z page boundaries with populations `9/3/3/1`.
- Removed exact indices `0,1,2` and `0,3,12` in fresh otherwise-equal arms,
  with strict server validation, ACK, and client air-block oracles.
- Proved membership thirteen in both arms and event rebuilds one versus three
  while cached pages/calls remain three and singleton fallback remains one.
- Bound each topology to a 44-byte post-seal artifact and complete M74/M78
  records; additive cost and performance direction remain outside the claim.

The frozen M84 semantic SHA-256 is
`ab9789101de12052aa945af741a37394c4a4b06fb78fa2d3d0737120a45eb39b`.

## 1.71.0 - M83 Page Topology Contrast

Status: GO for constant-two-member same-page versus cross-page structural
transitions over one fixed two-page scene.

- Removed exact indices `0,1` and `0,4` in fresh otherwise-equal arms with
  strict server validation, ACK, and client air-block oracles.
- Proved membership fourteen in both arms, but event rebuilds one versus two,
  while cached pages/calls remain two and fallback remains zero.
- Bound each topology to a 44-byte post-seal artifact and complete M74/M78
  records.
- Kept additive page cost, performance direction, causality, inference, and
  historical lag outside the claim.

The frozen M83 semantic SHA-256 is
`2418e988f23571a72a07c2521eb9ee7cb9ebc8b436957a74d7cf226fe4878f10`.

## 1.70.0 - M82 Natural Wave Ladder

Status: GO for a three-arm structural ladder over one fixed two-page Aero
scene, without a performance or dose-response claim.

- Ran fresh target-count arms 1, 2, and 4 with exact balanced target sets,
  server validation, acknowledgements, and client air-block oracles.
- Proved membership `15/14/12`, event rebuilds `1/2/2`, two cached pages/calls,
  two flush calls, and zero fallback.
- Bound each arm to a 44-byte post-seal sidecar and complete M74/M78 records.
- Kept additive cost, arbitrary topology/cardinality, causality, regression,
  improvement, inference, and historical lag outside the claim.

The frozen M82 semantic SHA-256 is
`2727138a7c9b2eb9e38b7a40a9ae8518a3c3c7b0739c188d2ae152edbbb47bab`.

## 1.69.0 - M81 Natural Multipage Rebuild

Status: GO for one server-authored two-cell change spanning two natural Aero
pages in the synchronized sixteen-identity scene.

- Fixed the same-plan fixture across Z=31/32 and removed exact indices zero and
  eight after retained record 300 with one typed request and acknowledgement.
- Proved both client blocks became air, membership `16 -> 14`, exactly two
  page rebuilds, two cached pages/calls, two flush calls, and zero fallback.
- Cross-bound a 40-byte request/event artifact to complete M74 and M78 records
  across two fresh same-plan/nonce replicas.
- Kept arbitrary topology, additions, repeated/dense waves, causal cost,
  regression, and historical lag outside the claim.

The frozen M81 semantic SHA-256 is
`f30116757d3fcf070289bdb013181744abdaf8da806426cc2efc76128484bc6d`.

## 1.68.0 - M80 Natural Membership Rebuild

Status: GO for one server-authored content removal and the corresponding real
Aero membership rebuild over the synchronized sixteen-identity scene.

- Sent one typed removal request after record 300 and required exact server
  validation, acknowledgement, and an air block at the client coordinate.
- Proved live renderer/enqueue membership `16 -> 15`, one page rebuild, two
  cached pages/calls, two flush calls, and zero direct fallback.
- Cross-bound a 36-byte request/event artifact to complete M74 and M78 records
  across two fresh same-plan/nonce replicas.
- Kept legacy stale block-entity cleanup, generic invalidation, dense waves,
  causal cost, regression, and historical lag outside the claim.

The frozen M80 semantic SHA-256 is
`3df82b51703daacc031e1f745f86fc7af6678d2da74901eb6c00183915e8a77a`.

## 1.67.0 - M79 Cold Page Rebuild

Status: GO for one explicitly armed Aero cell-page cache disposal and rebuild
over the synchronized sixteen-entity scene.

- Preserved the Aero-free common/server class closure while accessing the exact
  renderer model only from a client Mixin.
- Fired once after 300 retained records and required cache `4 -> 0 -> 4`,
  deleted/compiled deltas of four, four rebuilds/calls, and zero fallback.
- Required every other record to remain on M78's warmed rebuild-free path and
  cross-bound a 68-byte cold artifact to the M74/M78 artifacts.
- Ran two fresh same-plan/nonce replicas without an automatic-invalidation,
  causal, regression, or historical-lag verdict.

The frozen M79 semantic SHA-256 is
`94b95453ff0ba5944e7592bbdd8251c064dd0d7aa966cfa2c8b343ce92267d08`.

## 1.66.0 - M78 Paged Stage Timing

Status: GO for real steady-state Aero cell-page enqueue/flush acquisition over
the synchronized sixteen-entity scene.

- Added a client-only Aero marker while retaining an Aero-free common/server
  block-entity class closure.
- Required exact per-record `16` enqueues, `4` cached pages/page calls, zero
  direct fallbacks/rebuilds, and the corresponding M74 structural counters.
- Bound direct renderer/enqueue/flush spans and page counters to every M74
  census index in a post-seal versioned sidecar.
- Ran two fresh same-plan/nonce replicas without a cold-build, causal,
  regression, or historical-lag verdict.

The frozen M78 semantic SHA-256 is
`dbb52fb098cf377aa90027c4000ab7073efa6cbe5bc4f4fa56fa2090d38ae894`.

## 1.65.0 - M77 Direct Stage Timing

Status: GO for index-aligned direct synchronous renderer, Aero enqueue, and
flush spans over the constant sixteen-entity complete census.

- Added preallocated primitive timers around the full renderer, sixteen nested
  `queueAtRest` direct-fallback calls, and two empty-page flush calls per record.
- Wrote a versioned sidecar only after the M74 bracket sealed and cross-bound
  every record to the M74 nonce, plan, count, elapsed time, and fixture state.
- Preserved sub-clock-resolution flush aggregates as zero while requiring a
  positive full series and exact call cardinality.
- Ran two fresh same-plan/nonce replicas and reported descriptive spans without
  an uninstrumented-cost, causal, regression, or historical-lag verdict.
- Clarified that the server-safe plain BE does not implement Aero's paging
  marker, so M77 does not claim cell-page enqueue or populated-flush timing.

The frozen M77 semantic SHA-256 is
`4ac829480cfb8a9409d89c35e002246e43a0a143815303e1ac520e8990988a4c`.

## 1.64.0 - M76 Renderer Decomposition

Status: GO for exact complete-census acquisition across renderer-absent,
renderer-only, and renderer-plus-Aero treatments over one constant scene.

- Removed the renderer registration only after readiness for the `0/0` arm.
- Preserved sixteen renderer calls while independently suppressing or forwarding
  all sixteen nested Aero calls for exact `16/0` and `16/16` records.
- Fixed and runtime-checked vanilla max framerate plus disabled Aero pacing.
- Ran two fresh mirrored triplets and reported mixed descriptive stage deltas
  without a stable-cost, causal, regression, or historical-lag verdict.

The frozen M76 semantic SHA-256 is
`973ae93f8127bae80ceeddc372713f5968213aa1f2fb3a8978c58af61439ac40`.

## 1.63.0 - M75 Aero Exposure Ladder

Status: GO for exact complete-census acquisition at nested Aero call levels
`0/1/4/16` over one constant synchronized scene.

- Held sixteen server-authored block entities, network state, camera, plan, and
  renderer dispatches constant while varying only the Aero queue boundary.
- Required every binary record to contain sixteen dispatches and exactly the
  configured number of real at-rest renders and list calls.
- Ran two fresh mirrored ladders in forward and reverse order.
- Reported level-minus-zero summaries without a monotonicity, dose-response,
  causal, regression, or historical-lag verdict.

The frozen M75 semantic SHA-256 is
`92c9e4e28b17dd1df6750e5aff15022619211a1e981ffb9c3ccea461a3d9da05`.

## 1.62.0 - M74 Complete Aero Census

Status: GO for bounded complete renderer-interval acquisition over the paired
zero-versus-sixteen Aero-content fixture.

- Added a fixed-capacity primitive HEAD-to-HEAD interval recorder after exact
  fixture readiness, without retained per-sample allocation or I/O.
- Disabled the selective Aero logger and reset pinned at-rest counters through
  a test-only invoker at each measured renderer HEAD.
- Wrote one versioned binary artifact only after sealing and reparsed every
  record, aggregate, treatment state, plan, and nonce fail closed.
- Reported whole-census summaries and pair deltas descriptively, without a
  causal, inferential, regression, density, or historical-lag verdict.

The frozen M74 semantic SHA-256 is
`2cc4533688aa06ba1d69309639c36e16688b09eb4deeeb27d044277550d2d1a7`.

## 1.61.0 - M73 Paired Aero Content

Status: GO for balanced absent/present acquisition of sixteen synchronized Aero
content instances.

- Added one exact post-warm-up activation plus tracked-plan readiness handshake
  shared by both paired arms.
- Qualified zero placement/rendering in absent arms and sixteen exact synchronized
  renderer identities in present arms through explicit per-cell content messages.
- Ran two fresh balanced pairs with fixed time/frame windows and async logger files.
- Reported selected-row summaries and mixed pair deltas without an inferential or
  performance verdict.

The frozen M73 semantic SHA-256 is
`41422dda87ca7a8ed192e8c23c9946c55518f87e123cf69d6b1662d689b3b500`.

## 1.60.0 - M72 Aero Server Content

Status: GO for one exact server-authored custom block/entity rendered by Aero.

- Added a server-safe StationAPI content closure with no Aero/client imports.
- Sent distinct server-only state nonces through an explicit M72 content message.
- Bound identifier, raw ID, coordinates, block-entity type, and nonce client-side.
- Invoked the real pinned Aero at-rest renderer and completed twenty later frames.
- Repeated the boundary in two fresh modded server/client/worktree sets.

The frozen M72 semantic SHA-256 is
`6dff186ed904bdce57466038dd32a9824888d6de7ddb1a20041663cb8cec0501`.

## 1.59.0 - M71 Paired Aero Window

Status: GO for balanced paired acquisition and descriptive selected-row summaries.

- Added four fresh matched control/event pairs in balanced order.
- Anchored both arms to one exact Packet3 broadcast observed by the real client.
- Required exact combat absence in control and Packet18-before-Packet38 order in event.
- Captured fixed warmup/window bounds and at least thirty strictly parsed Aero rows per arm.
- Reported per-arm summaries and event-minus-control pair deltas without inferential classification.

The frozen M71 semantic SHA-256 is
`0b26d07ed6b08195a067bf8730b43f49ec596dae274c74f335f8a44576cb1d2b`.

## 1.58.0 - M70 Aero Combat Window

Status: GO for ordered combat-event observation and subsequent Aero frames.

- Composed the M66 combat fixture with the M68 real graphical observer.
- Sent the M69 swing immediately before the M66 attack request.
- Applied named Packet18 before victim Packet38 on the observer stream.
- Captured twenty post-event frames and strictly parsed post-event Aero rows.
- Allowed official reuse of destroyed dropped-item IDs while rejecting live duplicates.

The frozen M70 semantic SHA-256 is
`977bf908fc7edf5e0cf707f81fffaf6208183440a0f07cca81e2b9a22d03e571`.

## 1.57.0 - M69 Named Peer Swing

Status: GO for one isolated Packet18 request and named peer observation.

- Added a cumulative peer-swing session without changing M66 attack semantics.
- Froze the production Packet18 encoder as an exact six-byte message.
- Correlated animation 1 to Packet20 identity after Packet5 sword bootstrap.
- Repeated the official-server boundary and preserved clean persistence.

The frozen M69 semantic SHA-256 is
`4362b6b5b0cffbbf3429c6cfdad25ff3e077ed5be9a3f7e2f729f3806b9b69b3`.

## 1.56.0 - M68 Aero Multiplayer Login

Status: GO for real StationAPI/Aero client and vanilla multiplayer composition.

- Connected a real graphical b1.7.3 client through production `ConnectScreen`.
- Observed Packet1, first Packet13, and applied Packet51 remote readiness.
- Completed twenty post-ready renderer updates with post-ready Aero logs.
- Repeated clean client disconnect/server shutdown from a pinned clean checkout.

The frozen M68 semantic SHA-256 is
`a7978b0bb7e1277d846528036ff3ded3c5541ea5b11bd0935d32580b574e969f`.

## 1.55.0 - M67 Chest Retrieval

Status: GO for exact single-chest retrieval and final-state persistence.

- Added a bounded chest-to-player retrieval contract for exact stone.
- Committed active63, personal45, and cursor state only on matching Packet106.
- Reopened the stored chest before retrieval and closed through M58 proofs.
- Proved empty chest0 and exact personal36 stone after a clean restart.

The frozen M67 semantic SHA-256 is
`cbeb29b97d06faa167bb524366feb7b9d1a92fa03edeb432470d7f1ff0a7b469`.

## 1.54.0 - M66 Player Combat

Status: GO for one bounded armored PvP strike.

- Added username-resolved Packet7 attack requests without exposing raw IDs.
- Correlated a fresh target Packet38 on the attacker stream.
- Ordered victim-local Packet38 before Packet8 health `20 -> 18`.
- Proved diamond-sword wear `0 -> 1` and persisted victim health 18.

The frozen M66 semantic SHA-256 is
`8d05a812d9bfa62ac53321d1cca3f96c2cf9ff76668e36cdf0605945b883022c`.

## 1.53.0 - M65 Peer Armor Equipment

Status: GO for exact leather equipment and named-peer observation.

- Added typed leather moves into personal-window armor slots 5..8.
- Froze all eight Packet102 messages through the production encoder.
- Correlated the reversed armor layout with peer Packet5 slots 4..1.
- Proved Packet104, Packet5 bootstrap, and four NBT entries after restart.

The frozen M65 semantic SHA-256 is
`7bf03514d4331779e14ecaf3379ecf89d3bea276115ca77e909e5a9160587fe4`.

## 1.52.0 - M64 Workbench Output

Status: GO for exact workbench output, consumption, and persistence.

- Confirmed M63's modeled result with accepted slabs44x3:2 prediction.
- Required Packet200 crafted statistic 16842796 with increment three.
- Consumed result/matrix and stored exact slabs in personal slot 36.
- Closed safely and reopened after restart with persistent player/workbench state.

The frozen M64 semantic SHA-256 is
`fa5b92b7450d785451e527f7ecbab2597f99e0b9977b31333541a4e0a155253b`.

## 1.51.0 - M63 Workbench Preparation

Status: GO for bounded three-wide workbench matrix preparation.

- Added adapter-owned left-take and right-place Packet102 actions.
- Correlated the modeled row and cursor count transitions to each accepted ACK.
- Kept pressure-plate/slabs result values explicitly modeled until M64.
- Rejected Packet101 close while workbench result or matrix remains occupied.

The frozen M63 semantic SHA-256 is
`9fd2fb1869b8221cc5e2c9173a548224fb65ca6c6dc9c37858eeb88cd24bf289`.

## 1.50.0 - M62 Workbench Window

Status: GO for typed workbench open/read and safe empty-grid close.

- Preserved Packet100's exact `Crafting` descriptor and declared count of nine.
- Modeled the separate result slot and exact 46-slot Packet104 combined view.
- Reconciled personal slot 36 with combined slot 37 through one layout offset.
- Rejected workbench close unless result, matrix, and cursor are empty.

The frozen M62 semantic SHA-256 is
`975a1e57c412953d693d00c7a6105b5cbdfed428ab8bdc5e58a4ce04dd974fdf`.

## 1.49.0 - M61 Furnace Output Retrieval

Status: GO for exact glass retrieval and restart persistence.

- Continued the M60 container epoch with accepted actions 5 and 6.
- Required the exact glass Packet200 crafted-stat side effect before commit.
- Reconciled output, combined player tail, window 0, and cursor atomically.
- Reopened after restart with personal glass and an empty furnace output.

The frozen M61 semantic SHA-256 is
`3759ec0bd9b8f31341f5c783a82f30592ab69bc97a54da45bd14708f781ff51c`.

## 1.48.0 - M60 Furnace Smelt

Status: GO for exact live furnace loading and smelt observation.

- Added typed Furnace/3/39 remote-window decoding and exact tail reconciliation.
- Loaded sand and coal through four correlated accepted container actions.
- Reconciled asynchronous furnace Packet103 updates into the active window.
- Qualified cook/burn Packet105 progression through exact glass output.

The frozen M60 semantic SHA-256 is
`4d18743104fc8bb5efa84e46268323c5d77af8d121e315b156ea3305cf69b5de`.

## 1.47.0 - M59 Chest Transfer and Restart

Status: GO for accepted player-to-chest transfer and restart persistence.

- Added immutable two-action chest-transfer evidence.
- Reconciled the combined 63-slot view, canonical window 0, and cursor atomically.
- Reset action IDs by window-open epoch rather than reusable numeric ID.
- Reopened a fresh official server process and observed the persisted chest slot.

The frozen M59 semantic SHA-256 is
`4f1bfe9bca33138e8c833162aba2e62e1b120488dac8af034d47b60d10c73c9a`.

## 1.46.0 - M58 Remote Window Lifecycle

Status: GO for explicit remote-window close and confirmed personal restoration.

- Added immutable remote-window closure evidence.
- Sent Packet101 only for the exact locally active window and an empty cursor.
- Confirmed server closure through an accepted no-op Packet102 on window 0.
- Proved later personal transactions, peer held-state, and saved inventory.

The frozen M58 semantic SHA-256 is
`d74f622bc7b86332ec099b367830281038962f547c1a3d80a293a2e56a2ceda4`.

## 1.45.0 - M57 Personal 2x2 Crafting

Status: GO for the bounded personal log-to-planks recipe.

- Added immutable four-action personal crafting evidence.
- Predicted the 2x2 grid/result locally and committed each step only on Packet106 true.
- Reused rejected-transaction recovery for an authoritative empty-grid/planks audit.
- Proved terminal peer-held planks and one saved player-inventory entry.

The frozen M57 semantic SHA-256 is
`a7ca218db3ec5f4fe14ee8f7ec54955d49eb343c9185c62ab6982add0a2e8c7d`.

## 1.44.0 - M56 Rejected Transaction Recovery

Status: GO for rejected personal-click reconciliation and transaction re-enable.

- Added typed immutable rejected-transaction recovery evidence.
- Sent the exact Packet106 true re-enable ACK immediately after Packet106 false.
- Staged Packet104 and cursor Packet103 before atomic authoritative replacement.
- Proved recovery by a subsequent accepted action 2 and one saved inventory entry.

The frozen M56 semantic SHA-256 is
`707a15cd2055ee67795cf2d074d648e4395d644024015ef7ba999fd3c000f85b`.

## 1.43.0 - M55 Accepted Personal Transaction

Status: GO for accepted personal-window left-click transactions.

- Added immutable accepted personal-transaction values and a bounded session API.
- Encoded exact Packet102 action 1/2 predictions for take and place transitions.
- Committed staged slot/cursor state only after matching Packet106 true ACKs.
- Proved server state through peer Packet5 stone/empty/stone and one saved entry.

The frozen M55 semantic SHA-256 is
`c9abcffdd4d7663f0ce225d94bb59f73b07c632512e751f8c403f22ed0e2320e`.

## 1.42.0 - M54 Chest Window

Status: GO for a single-chest descriptor and immutable combined-window read.

- Added neutral immutable remote-window descriptor and container values.
- Corrected Packet100 decoding to its exceptional modified-UTF title format.
- Correlated type 0, title `Chest`, and 27 owned slots with a 63-slot Packet104 view.
- Proved the empty combined view after authoritative chest placement in two fresh worlds.

The frozen M54 semantic SHA-256 is
`c3fe36b177bb6263b467d92726ec430f16fc832f012417a1d5cd20be269a038f`.

## 1.41.0 - M53 Held Block Placement

Status: GO for selected held-block placement with two independent world views.

- Added neutral block faces and a bounded held-block placement session contract.
- Derived the Packet15 stack from the selected authoritative inventory slot.
- Proved Packet53 stone replacement in two immutable remote-world caches.
- Confirmed Packet103/Packet5 consumption and zero clean saved inventory entries.

The frozen M53 semantic SHA-256 is
`3b27d76f04b4e55d0c3197a091a0b98b39a0f9a5fdeee3b34b92f725e91e2472`.

## 1.40.0 - M52 Named Item Collection

Status: GO for exact named collection with terminal removal evidence.

- Added an immutable completed item-collection value and bounded session wait.
- Correlated Packet21 item IDs through Packet22 collector and Packet29 removal.
- Unified local login and remote Packet20 identities in a bounded item coordinator.
- Proved Packet103/Packet5 inventory restoration and one clean saved entry.

The frozen M52 semantic SHA-256 is
`905fe8b02bdc2f81e2280d4658b81440e4d975e6d52ff83a4fd573d0ad8f77af`.

## 1.39.0 - M51 Dropped Item Spawn

Status: GO for immutable dropped-item observation from an independent peer.

- Added a neutral immutable dropped-item value and bounded observation session.
- Strictly decoded Packet21 stack, fixed-point position, and signed velocity.
- Proved the spawn near the dropping actor with non-zero bounded launch motion.
- Retained independent local, peer-held, and clean persistence empty evidence.

The frozen M51 semantic SHA-256 is
`6051025c444760d21cf5a283358b4594612188234b72c7ae363c0a50d907e92f`.

## 1.38.0 - M50 Drop Held Item

Status: GO for drop-current-item with local and independent peer evidence.

- Added an explicit empty held-item value and bounded drop session contract.
- Sent the original Packet14 status-4 drop-current-item action.
- Proved Packet103 local-slot and Packet5 named-peer empty transitions.
- Confirmed zero remaining inventory entries after clean disconnect and save.

The frozen M50 semantic SHA-256 is
`f47c950ee765fa26735061bdf45cbbafbe66a0c8f8251dbd713bcc7c44ec4f3f`.

## 1.37.0 - M49 Held Item Peer Observation

Status: GO for bounded held-slot selection with independent peer evidence.

- Added a neutral held-item value and held-item multiplayer session contract.
- Sent Packet16 only for hotbar indexes 0 through 8.
- Correlated Packet20 named spawns with Packet5 carried-item updates.
- Proved slot-1 dirt selection through a second client on two fresh servers.

The frozen M49 semantic SHA-256 is
`df1873f6f3d7c48c3b34a400cad1a86a6579378b4b25cd5c99d90dcf63453039`.

## 1.36.0 - M48 Server Inventory Observation

Status: GO for bounded server-authoritative inventory observation.

- Added immutable neutral item-stack, indexed-slot, and inventory-window types.
- Decoded Packet104 full windows and applied matching Packet103 slot deltas.
- Proved an empty 45-slot player window followed by a real stone pickup in slot 36.
- Independently confirmed the observed stack in persisted player NBT.

The frozen M48 semantic SHA-256 is
`a501a36c74fa73d37995c8da8050f0718539e38db187539808e6fc491ba55abb`.

## 1.35.0 - M47 Immutable Batch Counts

Status: GO for bounded aggregate batch counts.

- Added immutable completed-route, outcome, and correction counts.
- Computed counts once from immutable route results without replaying events.
- Preserved execution and terminal-event identity.
- Proved exact `2 routes / 3 outcomes / 0 corrections` on two fresh servers.

The frozen M47 semantic SHA-256 is
`5937694a83f953037612da32bd49301d7413eedfe4aab84df98f341cc686bb5f`.

## 1.34.0 - M46 Exact Batch Terminal Event

Status: GO for identity-bound batch terminal summaries.

- Added `EVENT`, `AFTER_ROUTE`, and `EXHAUSTED` terminal kinds.
- Bound every batch result to its exact final indexed correlated event.
- Preserved the M45 result API through delegation to the richer execution.
- Proved all three terminal boundaries across two fresh official servers.

The frozen M46 semantic SHA-256 is
`23e11f826866e54447461ec94740a5e77d76abad7761fabcdf08d0ae5108e521`.

## 1.33.0 - M45 Event-Boundary Batch Stop

Status: GO for batch-wide cancellation at a movement event boundary.

- Added a synchronous batch event controller distinct from after-route control.
- Applied `STOP` immediately after the indexed event and before later movement.
- Proved one resolved outcome, absent later alternative, and absent later plan
  across two fresh official servers.
- Added no rollback, async delivery, parallelism, registry, retry, or adapter change.

The frozen M45 semantic SHA-256 is
`84d799547e96d434049f4879778606a592b3159626bf9df9b7e8225aeb9ca5d6`.

## 1.32.0 - M44 Synchronous Batch Observation

Status: GO for stable-index caller-thread batch observation.

- Added immutable batch events and a non-controlling synchronous observer.
- Indexed routes independently while preserving embedded alternative/outcome
  indexes and caller-owned correlation identity.
- Proved two exhausted routes, exact event order, cache coherence, and final
  persistence across two fresh official servers.
- Added no asynchronous delivery, parallelism, registry, retry, or adapter change.

The frozen M44 semantic SHA-256 is
`67a4fbc25b7288613c49431a9137a7104293d3262d7bd5898cbd0472b516287b`.

## 1.31.0 - M43 Bounded Correlated Route Batch

Status: GO for sequential correlated-route batch control.

- Added immutable correlated route plans and batch results with a 16-plan cap.
- Preserved each route's correlation, terminal event, and termination reason.
- Applied a synchronous batch `STOP` before the next unsent plan.
- Added no parallelism, registry, retry, scheduling, or adapter change.

The frozen M43 semantic SHA-256 is
`3b09e9188cd0948cb17f11f3f203888bfd04845bf599ea20fbd004b1d1a94e44`.

## 1.30.0 - M42 Caller-Owned Route Correlation

Status: GO for identity-preserving opaque route correlation.

- Added correlated event, controller, and execution wrappers around the M41
  route boundary.
- Preserved the exact caller-owned reference by identity in every synchronous
  event and the terminal summary.
- Proved correlated safe movement, terminal stop, and later-movement absence
  across two fresh official servers.
- Added no global registry, serialization, value interpretation, retry,
  scheduling, or adapter change.

The frozen M42 semantic SHA-256 is
`0256ed450183c49365c4ba2475f49203c7f5a1c180caefa5adf017cf87250237`.

## 1.29.0 - M41 Immutable Route Termination

Status: GO for exact stopped-versus-exhausted route summaries.

- Added immutable `MovementRouteExecution` and `MovementRouteTermination`
  values without changing the M39 or M40 entrypoints.
- Bound every summary to its exact final event and identical final outcome.
- Proved `CONTROLLER_STOP` after a fallback and `EXHAUSTED` after a complete
  route across two fresh official servers.
- Preserved later-movement absence, remote cache coherence, and player
  persistence without goal inference, retry, scheduling, or adapter changes.

The frozen M41 semantic SHA-256 is
`f3134a8e626058fc196b5ad3787199c6e0cd7f71a25a8a5db228289b886cdf7a`.

## 1.28.0 - M40 Observer-Directed Route Control

Status: GO for synchronous event-directed route cancellation.

- Added `MovementRouteController` and explicit `CONTINUE`/`STOP` directives
  without changing the M39 observation API.
- Applied each decision immediately after its immutable indexed event and
  before any fallback or later alternative could be sent.
- Proved a corrected primary, accepted fallback, synchronous stop, and absent
  later alternative across two fresh official servers.
- Preserved event/outcome identity, remote cache coherence, and final player
  persistence without an executor, queue, or adapter change.

The frozen M40 semantic SHA-256 is
`6a3285b118eccd8b3f1e95ba51e7f6de46933c168b9f56f2623b11d8d266da7b`.

## 1.27.0 - M39 Synchronous Route Observation

Status: GO for caller-thread indexed route outcome observation.

- Added immutable `MovementRouteEvent`, `MovementAttemptKind`, and a synchronous
  `MovementRouteObserver` boundary.
- Emitted each primary/fallback event immediately after its bounded movement
  resolved, with stable alternative and outcome indexes.
- Preserved object identity between observed outcomes and the immutable final
  route result across two fresh official servers.
- Added no thread or asynchronous game callback; cache and persistence remained
  coherent through the observed route.

The frozen M39 semantic SHA-256 is
`df2973b510807bc1ebce5b49ba2921e14137bd2970fba351d61df46f44165222`.

## 1.26.0 - M38 Explicit Movement Fallback

Status: GO for caller-supplied single fallback after correction.

- Added immutable neutral `MovementAlternative` primary/fallback pairs with a
  bounded 32-pair route entrypoint.
- Skipped fallback after an unchallenged primary and executed exactly one
  fallback after a corrected primary.
- Proved the exact `UNCHALLENGED`, `CORRECTED`, `UNCHALLENGED` outcome sequence
  on two fresh official servers without retrying the blocked primary.
- Preserved the remote cache and persisted the explicit fallback's final pose.

The frozen M38 semantic SHA-256 is
`850b6e29ed5e8aab12e48625ebde6b8ce1902b581d9e07f55c8488f2d7bfd947`.

## 1.25.0 - M37 Route Correction Policy

Status: GO for explicit retry-free stop-on-correction routing.

- Added neutral `RouteCorrectionPolicy` values for continued and stopping
  route execution.
- Preserved M36's continue behavior as the default overload.
- Stopped a three-step route immediately after its second, corrected step on
  two fresh official servers, producing exactly two outcomes and one correction.
- Required zero retry, cache retention, and persisted proof that the third step
  was never applied.

The frozen M37 semantic SHA-256 is
`4a9a43b61c171fd05ab6156b07c963b7c1ebcdedc6ab7ea42d7a40db04cdf649`.

## 1.24.0 - M36 Route Recovery

Status: GO for bounded relative-route continuation after correction.

- Added immutable neutral `MovementStep` and `MovementRouteResult` values plus
  a recovering session contract with a bounded 64-step ceiling.
- Executed route steps relative to the latest resulting pose, including after
  a server-authoritative correction.
- Qualified the exact ordered outcomes `UNCHALLENGED`, `CORRECTED`,
  `UNCHALLENGED` on two fresh official servers.
- Required one correction, preserved the original cache chunk, and persisted
  the final recovered pose after clean disconnect.

The frozen M36 semantic SHA-256 is
`895c39dd8b5e5d0f18c7eac81b76c5da77df74b98ecb434aad93adf49cfbc0c8`.

## 1.23.0 - M35 Bounded Movement Outcome

Status: GO for bounded unchallenged/corrected movement classification.

- Added immutable neutral `MovementOutcome` and `MovementDisposition` values
  plus a resolved sustained-session contract.
- Classified a move as corrected only after an inbound Packet13 was consumed;
  absence during the bounded window remains explicitly `UNCHALLENGED`.
- Persisted a collision-safe `+0.125 X` move in player NBT on two fresh official
  servers, qualifying the live unchallenged evidence as accepted.
- Forced a solid-block collision afterward and required rollback to that last
  accepted pose while retaining the original decoded chunk.

The frozen M35 semantic SHA-256 is
`414c83fa237a0affd1c36ab171e04f07ab110487fc2ebd75698f54e55d92417a`.

## 1.22.0 - M34 Server-authoritative Pose Correction

Status: GO for correction decode, acknowledgement, and neutral pose convergence.

- Decoded server Packet13 with its server-side stance/feet field order and
  rejected invalid stance intervals.
- Acknowledged each correction in the exact client Packet13 field order before
  exposing it to the sustained neutral session.
- Deliberately moved into a solid block selected from the decoded cache on two
  fresh official servers and required exact convergence to the initial pose.
- Preserved the original cached chunk across correction; outbound invalid
  movement alone never counted as success.

The frozen M34 semantic SHA-256 is
`b62641c2a99876737d070566eb1330ab14a569e7e2f7a7ea66293e1e768a302f`.

## 1.21.0 - M33 Chunk Traversal Lifecycle

Status: GO for deliberate cross-chunk movement and rendered cache turnover.

- Rose eight blocks for collision-free clearance, then crossed two eastward
  chunk boundaries in bounded quarter-block movement steps.
- Preserved M30 strict prechunk qualification before movement, then enabled
  bounded implicit edge MapChunk loads observed from the official server.
- Required at least one immutable cached chunk removal and one decoded addition
  after the traversal on each of two fresh servers.
- Rendered before/after cache topologies through mapped Minecraft
  `Tessellator`, native LWJGL, and an offscreen Pbuffer.
- Required a removed chunk pixel to clear, an added chunk pixel to appear, and
  the complete RGBA frame hash to change.

The frozen M33 semantic SHA-256 is
`8f2860494fba146931fbe768d01a5c0dc063d05cc2ac01afd3fa9cce4c8b7e0d`.

## 1.20.0 - M32 Sustained Remote Terrain Render

Status: GO for sustained protocol-14 cache-to-native-render composition.

- Added a neutral 40-tick sustained remote-world session contract.
- Reproduced the unchanged vanilla cadence byte-for-byte: 38 flying packets
  and two periodic pose packets.
- Pumped a multi-chunk decoded view (at least four chunks) while keeping each
  session connected to an unmodified official server.
- Rendered cache-derived 8x8 terrain slices through mapped Minecraft
  `Tessellator`, native LWJGL, and an offscreen Pbuffer.
- Required Packet53 to turn both the exact cached block and its corresponding
  native frame pixel into air before accepting the update.

The frozen M32 semantic SHA-256 is
`7ca1a2fd0d3c4d172e3f123c1b1382a2b939c5ebe0a09e7570acf7a381399f00`.

## 1.19.0 - M31 Incremental Remote World

Status: GO for server-authoritative incremental block updates.

- Added neutral begin/finish break intent and exact expected-block waiting.
- Decoded Packet53 single-block and Packet52 packed multi-block updates into
  immutable replacement snapshots while preserving prior values and light.
- Proved exact mapped coordinate/state application in a deterministic fixture.
- Broke one nearby block in each of two fresh official servers and required the
  inbound target state `0:0`; outbound intent alone never counted as success.
- Bounded exact-state waits by elapsed time even while keepalives arrive, and
  ensured failed server boots terminate their child process.
- Kept mining prediction, drops, full client heartbeat, entities, rendering,
  and server tick stepping as later milestones.

The frozen M31 semantic SHA-256 is
`f238ca0cb8dc430ba88e17dc25425d158569d08d7dc9abda01b97cdc87cde6bf`.

## 1.18.0 - M30 Remote World Cache

Status: GO for bounded prechunk-qualified remote-world caching.

- Added immutable `RemoteWorldView` and `CachedRemoteWorldMultiplayerSession`.
- Unified pose/chat/chunk inbound consumption so native Packet50 load/unload
  lifecycle is retained while other packet types are awaited.
- Required a load reservation before accepting decoded Packet51 data, evicted
  it on unload, and enforced a hard 256-region bound.
- Consumed partial Packet51 regions without caching them, keeping incremental
  range application outside this milestone.
- Proved two-chunk cache semantics in the lifecycle oracle, negative-safe world
  addressing, and one qualified full chunk from each of two official servers.
- Kept incremental block/entity updates, native world construction, rendering,
  and server tick stepping as later milestones.

The frozen M30 semantic SHA-256 is
`efa8065f90fda3c466ccdf7c22d1b54b8a6470fbb61354176467635f3e980631`.

## 1.17.0 - M29 Remote Chunk Snapshot

Status: GO for strict native chunk inflation and neutral block access.

- Added immutable `RemoteChunkSnapshot` and `RemoteWorldMultiplayerSession`.
- Required exact bounded zlib completion and split native payloads into block
  ID, metadata, block-light, and sky-light planes inside the b1.7.3 adapter.
- Compiled mapped vanilla `NibbleArray` from the pinned local workspace and
  compared every coordinate of a synthetic full chunk without vendoring source.
- Decoded real full chunks from two fresh official client/server scenarios.
- Kept prechunk lifecycle, multi-chunk caching, entities, native world
  construction, rendering, and server tick stepping as later milestones.

The frozen M29 semantic SHA-256 is
`aec53757fe91829f4e425428a590b703595088ed02955b01ba41179ed4969b0b`.

## 1.16.0 - M28 Remote Chunk Observation

Status: GO for bounded native chunk-envelope observation.

- Added immutable `RemoteChunkObservation` and a chunk-capable multiplayer
  session extension.
- Parsed official `Packet51MapChunk` origin/dimensions and consumed its bounded
  compressed payload inside the b1.7.3 adapter.
- Repeated two fresh client/server scenarios and required complete
  `16 x 128 x 16` remote regions.
- Kept spawn-dependent origins and compressed sizes observational.
- Kept decompression, block/world construction, chunk caching, and server tick
  stepping as later milestones.

The frozen M28 observation SHA-256 is
`45179dd32117513e55cbf0698ec09e51440b3e3007188c100bcdd234257f0be4`.

## 1.15.0 - M27 Two-Client Multiplayer Chat

Status: GO for native peer chat through a bounded inbound packet pump.

- Added `ChatMultiplayerSession` for bounded chat send and receive.
- Added a fail-closed protocol-14 inbound codec for qualified login/play packet
  lengths, including chunk, entity, metadata, inventory, and block traffic.
- Connected two clients simultaneously to each official server and required
  exact two-player presence.
- Sent `worldline-m27` from `WorldlineA` and received the exact native broadcast
  on `WorldlineB` in two fresh scenarios.
- Kept chunk/world decoding, asynchronous pumping, remote-player rendering, and
  server tick stepping as later milestones.

The frozen M27 chat SHA-256 is
`7d264e3b365a4ab223d45cd95eb17aa90683ef123af51775defc120d7635aa12`.

## 1.14.0 - M26 Native Multiplayer Render Bridge

Status: GO for native offscreen rendering composed with a real multiplayer session.

- Combined official-server protocol-14 login/pose synchronization with mapped
  Minecraft `Tessellator` in one client process.
- Rendered connected state through native LWJGL and an OpenGL Pbuffer while
  requiring that no onscreen `Display` exists.
- Repeated two fresh client/server scenarios with exact pixel coverage and the
  M10-qualified mapped/official frame hash.
- Kept the complete Minecraft gameplay loop, chunk rendering, interactive GUI,
  and server tick stepping as explicit later milestones.

The frozen M26 bridge SHA-256 is
`c2d85227a2cb542e0c9b21aa77dd71a0bbfaab7162a1db6c0fb0955876dbb2ce`.

## 1.13.0 - M25 Multiplayer Player Movement

Status: GO for bounded movement accepted by the official server.

- Added relative movement intent to `PlayableMultiplayerSession`.
- Preserved the exact server-provided stance height when encoding native
  protocol-14 position/look packets.
- Used a within-spawn-block `+0.125 X` displacement independent of adjacent
  random terrain.
- Repeated two fresh official server scenarios and required exact target X/Y/Z
  in persisted player NBT.
- Kept arbitrary collision correction, continuous packet pumping, graphical
  client control, and server tick stepping as later milestones.

The frozen M25 movement SHA-256 is
`fb5715319d1347b180aea28652c173a9278d67dedbd3f6e9b486fe358d31f6d6`.

## 1.12.0 - M24 Multiplayer Play Pose

Status: GO for the bidirectional initial play-position exchange.

- Added immutable `PlayerPose` and a neutral playable multiplayer-session
  boundary for synchronization and look intent.
- Added a bounded protocol-14 play codec for spawn/time/chunk prelude packets,
  position decoding, native feet/stance acknowledgement, and client look.
- Extended persisted player observation with yaw and pitch from official NBT.
- Repeated two fresh official server scenarios and matched acknowledged
  position plus the exact requested `135.0/-22.5` rotation.
- Kept collision-qualified movement, the graphical client, continuous packet
  pumping, and server tick stepping as later milestones.

The frozen M24 play-pose SHA-256 is
`e43923f84231be276ae24a78a94f1d50aef3d5229dc59f10bcc5fd83c7cbc0db`.

## 1.11.0 - M23 Multiplayer Player Persistence

Status: GO for persisted multiplayer player observation.

- Added immutable `ServerPlayerState` and a persistent multiplayer server
  boundary for username, dimension, position, health, and inventory count.
- Added an original safe gzip/NBT reader for official server player files.
- Repeated two login/logout/save scenarios and verified bounded player state
  without freezing machine/world-dependent spawn coordinates.
- Kept movement, full play packets, graphical client, and tick determinism as
  later milestones.

The frozen M23 persistence SHA-256 is
`cce8512d97119d2c7fd010110a1760bebe7d86bed4f3d8cc1fefe39e58fb8928`.

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
