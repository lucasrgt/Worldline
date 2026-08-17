# Changelog

All notable Worldline changes are recorded here. The project follows semantic
versioning for declared stable contracts; experimental adapter APIs may change
before they are promoted.

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
