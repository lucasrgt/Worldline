# Worldline Architecture

This document defines the smallest architecture that can grow toward a
controlled Minecraft Beta 1.7.3 runtime without mixing the public driver,
runtime policy, and game-specific integration.

## Current slice

```text
caller
  |
  v
worldline-api        public lifecycle contract and value types
  ^
  |
worldline-kernel     lifecycle policy and the backend integration port
  |
  v
b173 adapter         mapped game-specific implementation and lab controls

worldline-trace      independent canonical observation protocol
  ^            ^
  |            |
subject        official-JAR oracle

bundle -> worldline-cli -> worldline-reproduction
                              |
                              v
                       ReplayProvider SPI
                              |
                              v
                         b173 adapter

trace files -> worldline-cli -> worldline-analysis -> worldline-trace

mod JAR -> worldline-cli -> worldline-mods
                              |
                              v
                   descriptor-selected entrypoint

mod + trace -> worldline-cli -> worldline-modtest -> worldline-analysis
                                        |                  |
                                        v                  v
                                  worldline-mods      worldline-trace

scenario -> worldline-cli -> worldline-minimization -> worldline-analysis
                                   ^
                                   |
                          adapter-owned evaluator

ItemCensus / GameUi.nodes() -> worldline-invariants

SemanticMapping -> worldline-semantics

OptimizationRef -> worldline-optimization -> owner-controlled catalog
```

The modules are physical source roots and are compiled separately. The API is
compiled with no product classpath. The kernel is compiled with only the API on
its classpath. Reproduction depends only on the API; CLI depends only on API
and the stable product modules it exposes. Analysis depends only on trace.
This makes every declared dependency direction executable rather than
conventional.

### `optimization`

Owns only the source-retained `OptimizationRef` metadata annotation. It has no
dependencies and performs no runtime work. Modules opt into this compile-time
dependency only when they annotate an owned optimization site. A separate
harness check validates records and references; it never enables features or
transforms bytecode. External projects keep their records beside their own
implementations. Worldline evidence may cite an external stable ID, but the
Worldline catalog must not describe mod-specific algorithms or flags.

### `api`

Owns stable concepts visible to a scenario author. It must not expose RetroMCP,
LWJGL, mappings, instrumentation, or decompiled Minecraft types.

The Game UI Tree adds opt-in `UiMinecraftRuntime` and immutable
`GameUiNode` values. `GameUi` exposes the current screen, a snapshot of semantic
nodes, role/name lookup, inventory open/close, and slot clicks. Only the
inventory screen is promoted; other screens fail closed.

M3 adds the opt-in `AutomatedMinecraftRuntime` extension and neutral
`GameWorld`, `GamePlayer`, and `GameEntity` handles. Coordinates and block
states are immutable API values. Live handles are lifecycle-guarded: they are
usable only while their runtime owns a loaded world, and retained handles fail
closed after the runtime closes.

M4 adds `SnapshotMinecraftRuntime` and the opaque, immutable
`RuntimeSnapshot` byte artifact. The API owns only bounded value semantics;
the b1.7.3 adapter owns its versioned replay format and restore interpretation.
This keeps mapped events and fingerprints out of the neutral public module.

M5 adds two separately compiled product modules. `worldline-reproduction`
owns the canonical bundle envelope and replay SPI; `worldline-cli` owns command
parsing and stable machine-readable output. It discovers the b1.7.3 provider at
runtime, so neither module depends on mapped Minecraft classes. The repository
launcher only assembles the already verified local runtime classpath.

M6 adds `worldline-analysis` above the existing trace protocol. The trace
module owns strict `v2` parsing and immutable data; analysis owns rendering and
first-divergence semantics. The CLI can therefore inspect trace files with no
adapter, game JAR, RetroMCP checkout, or native library on its classpath.

M7 adds dependency-free `worldline-mods`. It owns canonical package metadata,
bounded inspection, SHA-256 provenance, exact compatibility results, and the
generic isolated loader. The CLI depends on this module for metadata-only
inspection. At execution time an adapter supplies its own entrypoint interface,
so the package module never depends on b1.7.3 or mapped game types.

M8 adds `worldline-modtest` above mods, trace, and analysis. It owns the durable
provenance-bound result envelope and comparison metadata, while delegating trace
parsing and first-divergence order to their existing modules. The CLI records
and compares results without loading an adapter or executing mod code.

M9 adds `worldline-minimization` above analysis. It owns canonical opaque-step
scenarios, exact divergence fingerprints, and deterministic delta debugging.
Adapters remain responsible for step interpretation and isolated evaluation,
so the neutral module does not acquire game, mod, or runtime dependencies.

The semantic catalog adds `worldline-semantics` above the API.
It owns the closed 24-category role contract, static role graph, fail-closed
lookup, and adapter manifests. The API owns immutable `SemanticMapping`
values, including optional official client aliases. Category files annotate
controlled b1.7.3 symbols already evidenced by Worldline; unknown or
duplicate symbols fail closed. Adapter manifests bind Worldline-owned sites
to those roles. External libraries such as Aero stay out of the catalog and
may depend on Worldline later; Worldline does not depend on them.

The Invariant Engine adds `worldline-invariants` above the API.
It owns fail-closed rules and the observation loop. The API owns the immutable
`ItemCensus`, `EntityCensus`, `WearCensus`, `ItemCensusObserver`, and
`InvariantViolation`. The kernel's opt-in `watch` samples items, loaded
blocks, living entities, newly loaded chunk items and blocks, wear, health,
and world time after every tick. Item conservation accepts loss, transfers,
imports, recipes, and cause drops. Entity spawn accepts imports, eggs, and
breeding, grass/water/spawner hosts, and slime split. Block conservation
accepts imports, ID swaps, fluid/fire/plant presence, sapling-to-tree, and
cobble from fluid. Health conservation accepts food, cake bites, and
peaceful regen. Durability
conservation forbids repair without a new stack. `TimeMonotonic` forbids
time moving backward. There is no energy invariant.

### `kernel`

Owns control-flow policy: valid lifecycle transitions and the narrow backend
port that each Minecraft integration must implement. It must not claim that
a transition succeeded unless the backend call completed.

Kernel unit tests use a recording fake. The server smoke supplies a test-scoped
`VanillaWorldBackend` for direct world-level evidence. The client cycles use the
reusable `B173Runtime` adapter, which constructs the original client tick object
graph and reaches `Minecraft.runTick()` through the same port. The adapter has
its own per-file source ceiling and compiles against local, ignored Minecraft
classes; no game binary or decompiled source enters the repository.

### `trace`

Owns the versioned canonical trace grammar, input validation, SHA-256 signature,
and output framing shared by a subject and its oracle. It has no dependencies
on the API, kernel, RetroMCP, or Minecraft. Adapters collect observations; the
trace module only serializes supplied values and therefore cannot make both
sides agree on game behavior by itself.

## Artifact and toolchain provenance

`artifacts/` contains public descriptors, never game binaries. The b1.7.3
client and dedicated-server descriptors freeze byte length, SHA-1, and
SHA-256. Runtime verification accepts only matching local JARs under the
ignored `local/artifacts/` root. The acquisition tool downloads through a
partial file and installs an artifact only after all frozen identity fields
match.

`toolchains/` pins external open-source tooling by repository and immutable Git
revision. Bootstrap checkouts and builds live under `local/toolchains/`; no
RetroMCP source is vendored into Worldline.

## First executable vanilla smokes

`smokes/deterministic-world-tick/` compiles a deterministic in-memory save/chunk
fixture and a `GameBackend` adapter against the mapped vanilla server classes
produced by RetroMCP. Its driver uses the public `MinecraftRuntime` interface;
the kernel enforces lifecycle policy before the adapter reaches `World.tick()`.
The same eight-tick fixture is independently compiled against and executed by
the official obfuscated server JAR. Two headless JVMs on each side must be
internally deterministic, byte-identical across the boundary, and match the
committed SHA-256 signature. Both sides depend on `worldline-trace` for the
canonical `v1` format, removing duplicated protocol and hashing code without
sharing their Minecraft access paths.

This establishes controlled vanilla `World.tick()` execution and differential
equivalence for one narrow observed fixture.

`smokes/m20-server-bootstrap/` adds the dedicated-server process boundary. It
does not reuse mapped server classes: it starts the unmodified official server
JAR twice in fresh directories, confines each process to localhost, waits for
native readiness, sends the native stop command, and requires clean save/exit.
Generated properties, logs, worlds, and the server JAR remain ignored. Later
server-tick and multiplayer adapters build above this lifecycle proof.

M21 adds `DedicatedServerRuntime` and `ServerState` to the neutral API. The
`b173-server` adapter owns process startup, native stdin/stdout commands, and
the gzip/NBT reader for generated `level.dat`; callers never depend on
obfuscated server classes. Its smoke starts two controller/server pairs, sets
time, waits for save completion, observes persisted state, and closes cleanly.
The server remains natively ticking throughout, so this is control and
observation rather than external tick stepping.

M22 adds `MultiplayerSession`, `MultiplayerState`, and
`MultiplayerServerRuntime`. The b1.7.3 wire adapter owns the protocol-14 packet
encoding and localhost socket; neutral callers see only connection state,
username, protocol version, entity ID, and server player lists. The smoke uses
two fresh original wire clients against two unmodified official servers and
requires presence followed by clean absence. It intentionally stops before the
full play protocol or official graphical-client boundary.

M23 adds `PersistentMultiplayerServerRuntime` and immutable
`ServerPlayerState`. The b1.7.3 adapter resolves only validated usernames and
parses generated gzip/NBT player files without server classes. Its smoke proves
that two real login/logout sessions produce bounded persisted dimension,
position, health, and inventory observations; exact spawn coordinates remain
outside the frozen trace.

M24 adds `PlayableMultiplayerSession` and immutable `PlayerPose`. A separate
bounded play codec owns Beta 1.7.3's packet IDs, prelude lengths, and inverted
feet/stance acknowledgement; the neutral API sees only pose and look intent.
The persisted-player value now includes rotation. Unknown packet IDs and
invalid lengths fail closed rather than silently approximating a full client.

M25 extends only that neutral playable-session boundary with relative movement.
The adapter preserves the server-provided stance height and emits the original
position/look packet; the official server remains authoritative. The smoke uses
a within-block displacement, then observes native persisted NBT rather than
assuming that sending a packet means the server accepted it.

M26 composes the b1.7.3 server/wire adapter with the separately qualified native
render path. The smoke controls orchestration; a render-only helper owns LWJGL
and `Tessellator`. Connected protocol state selects the frame, but neither API
nor server adapter depends on graphics. M10's mapped/official renderer equality
is reused rather than duplicated as a new full-client claim.

M27 adds a chat-specific neutral extension and a separate inbound packet codec.
The play channel owns semantic chat send/receive while the codec owns only
qualified payload lengths and bounds. Two simultaneous wire sessions exercise
native server broadcast after queued entity/chunk traffic. No packet vocabulary
enters the neutral API, and payload skipping is not presented as world decoding.

M28 adds a chunk-observation extension without changing that ownership. The
inbound codec parses the native chunk envelope and consumes its bounded payload;
the API receives only origin, dimensions, and byte count. Compression format,
block layout, packet IDs, and mapped types remain adapter-side. Observation is
explicitly separate from decompression, caching, and world construction.

M29 adds an immutable neutral snapshot above that observation. The b1.7.3
adapter alone owns zlib completion and legacy four-plane/nibble interpretation;
the API exposes only coordinate-addressable block state and light values. A
runtime-compiled mapped `NibbleArray` provides the layout oracle without
vendoring decompiled source. The snapshot is isolated evidence, not a chunk
cache, entity stream, or native client world.

M30 introduces a bounded adapter-private lifecycle cache and immutable neutral
view. One shared play pump retains Packet50 load/unload state while callers wait
for pose, chat, chunks, or a minimum decoded world. Packet51 data cannot enter
the cache before a load reservation, unload removes it, and 256 tracked regions
is a hard fail-closed ceiling. Incremental updates and native world/render state
remain outside this layer.

M31 applies Packet52/53 changes by immutable snapshot replacement. Outbound
begin/finish dig packets express intent only; the cache changes exclusively
after an official inbound update reaches the shared pump. This keeps server
authority explicit and leaves mining prediction, drops, entities, and rendering
outside the neutral cache.

M32 adds a bounded synchronous play heartbeat: Packet10 carries unchanged
flying state on ordinary ticks, while every twentieth tick resends Packet13
pose state. Between ticks the same inbound pump accumulates lifecycle-qualified
chunks. A smoke-only renderer converts an exact cached vertical slice into
mapped `Tessellator` quads in native LWJGL; it does not move rendering policy or
Minecraft classes into the neutral API.

M33 composes `moveBy` and `sustainTicks` into quarter-block traversal. The cache
remains strict before deliberate movement; movement enables bounded implicit
MapChunk loads because the official server can stream a new view-edge Packet51
without a preceding Packet50. Packet50 unload still evicts immediately. Native
topology frames prove both sides of the cache transition without placing LWJGL
in the neutral API.

M34 promotes inbound Packet13 from skipped traffic to an adapter-owned
authoritative correction. The inbound pump decodes the server field order,
validates the stance interval, writes the required client-order acknowledgement,
and retains only the newest correction. `sustainTicks` then atomically replaces
the channel pose and stance before returning its immutable cache view. No
prediction, collision model, or mapped packet type crosses the neutral API.

M35 adds a neutral classification over that pump boundary. `moveAndObserve`
sends one movement intent, sustains a caller-bounded number of protocol ticks,
and returns the attempted pose, resulting pose, and whether an actual correction
arrived. No correction is named `UNCHALLENGED`, not accepted; only the official
persisted-player oracle upgrades the tested small move to acceptance evidence.

M36 composes bounded relative steps in a default neutral orchestrator. Each
step calls the M35 boundary, and the next delta therefore starts from whatever
pose the previous outcome produced, including an authoritative rollback.
Inputs and ordered outcomes are bounded to 64 and immutable at the public edge;
the adapter gains no route planner or collision model.

M37 makes post-correction control flow explicit with `CONTINUE` and
`STOP_ON_CORRECTION`. The default remains M36 continuation. Stop policy breaks
the neutral route loop immediately after recording the correction; it neither
retries the failed step nor sends later steps. The policy contains no hidden
path selection and requires no adapter change.

M38 adds explicit primary/fallback pairs above the same resolved movement
boundary. A fallback is conditional on an actual corrected primary and executes
at most once; an unchallenged primary skips it. Thirty-two pairs cap the worst
case at the existing 64-outcome ceiling. The orchestrator neither retries nor
derives an alternate movement and still requires no adapter change.

M39 emits immutable route events synchronously after each resolved primary or
fallback. Alternative and outcome indexes are bounded by the M38 ceilings, and
the event retains the exact outcome object later returned in the route result.
Observers run on the caller thread; no game callback, executor, queue, or
adapter behavior is introduced.

M40 adds a separate synchronous controller boundary so the non-controlling M39
observer remains source compatible. After each immutable event, the controller
returns an explicit `CONTINUE` or `STOP`; `STOP` is applied before any fallback
or later alternative. The controller cannot retract a resolved outcome, invent
a movement, or schedule asynchronous work, and requires no adapter change.

M41 wraps a controlled route in an immutable `MovementRouteExecution`. Its
termination is exactly `EXHAUSTED` or `CONTROLLER_STOP`, and its terminal event
is the same final event delivered synchronously to the controller, retaining
the identical last outcome object. This is a summary of completed work, not a
goal, scheduler state, inferred path, or adapter concern.

M42 decorates those events and the execution with one opaque caller-owned
reference. Correlation is preserved by object identity and is never inspected,
serialized, compared by value, or placed in a registry. The correlated wrapper
does not change route execution, controller timing, or adapter behavior.

M43 composes at most 16 correlated route plans sequentially. Each completed
execution retains its own route termination; an independent synchronous batch
controller can stop before the next plan. No plans execute concurrently and no
correlation registry or adapter behavior is introduced.

M44 adds a non-controlling synchronous batch observer. Its immutable event adds
only the bounded route index and retains the exact M42 correlated event, so
alternative/outcome indexes and correlation identity remain unchanged. Delivery
occurs on the caller thread before the existing route controller decision.

M45 adds a separate batch event controller. Its `STOP` becomes both the current
route's controller termination and the batch termination before any later
alternative or plan is sent. Already resolved outcomes remain immutable; no
rollback, concurrency, queue, or adapter behavior is introduced.

M46 wraps the batch result with an exact terminal kind and the last M44 batch
event. The event is the identical wrapper delivered at the final resolved
movement and retains the final execution's correlated event by identity.
Existing M45 callers still receive the original result through delegation.

M47 computes bounded aggregate counts once while constructing the immutable
batch result. Counts preserve the original execution graph and do not flatten,
replay, synthesize, or replace route outcomes or events.

M48 composes an independent inventory tracker into the protocol-14 inbound
pump. Packet104 atomically replaces the bounded immutable window; Packet103
replaces one slot only when its window and index match the current view.
Neutral API values represent legacy item stacks and explicit empty slots. Live
observation never reads server memory or persistence files.

M49 adds a separate peer-equipment tracker. Packet20 establishes the bounded
entity-ID-to-username correlation, while Packet5 slot zero supplies the
server-authoritative carried item. Packet16 selection remains a small outbound
channel action restricted to hotbar indexes 0 through 8. No shared registry or
server-memory access connects the two peers.

M50 extends only the held-item session boundary with a drop-current action.
The adapter writes Packet14 status 4 and relies on the official server for all
state transitions. Packet103 empties the actor's immutable inventory view;
Packet5 empties the independently named peer observation. Empty sentinels are
decoded strictly, and malformed negative item identifiers fail closed.

M51 composes a dedicated dropped-item tracker into the same bounded inbound
pump. Packet21 is decoded into a neutral immutable item entity with the exact
legacy stack, fixed-point coordinates, and signed-byte velocity. The tracker
retains only the latest spawn needed by the bounded wait; it is not a shared
entity registry, trajectory simulator, or server-memory view.

M52 extracts item traffic into `B173ItemInbound`, keeping the general inbound
pump below its per-file limit while composing inventory, peer equipment,
identity, and dropped-item trackers. Login identity seeds the local entity;
Packet20 binds remote names. A bounded item map correlates one exact Packet21
entity with Packet22 collector identity and later Packet29 removal. Destruction
without collection remains a valid terminal lifecycle but cannot satisfy the
collection contract.

M53 extracts outbound selected-item behavior into `B173HeldItemChannel`.
Packet16 selection, Packet14 drop, and Packet15 placement share one selected
hotbar index. Placement derives the exact stack from the current immutable
window-0 slot and rejects empty or non-block selections; callers supply only a
neutral support position and `BlockFace`. Packet53 and inventory trackers remain
the independent inbound authority.

`smokes/controlled-client-tick/` completes the client-level cycle. It invokes
the original `Minecraft` constructor, installs explicit headless boundaries,
loads an original client `World`, and executes exactly one externally requested
`Minecraft.runTick()`. Its independent oracle uses official client names and
the frozen official JAR. The runner checks class origins, bytecode call paths,
four-process determinism, cross-boundary equality, and a frozen trace. The exact
scope and non-claims are normative in that smoke's `MAP.md`.

M2 promotes the process boundaries used by that cycle: virtual clock,
programmable input, RNG reseed, in-memory filesystem journal and failure
injection, offline network, tick-keyed scheduler, and supervision of the
vanilla timer thread. Those controls stay on the b1.7.3 adapter; they are
not `worldline-api` types. The contract is `docs/M2_RUNTIME.md`.

`smokes/m3-domain-api/` qualifies the stable domain surface. The subject uses
only `AutomatedMinecraftRuntime` and neutral API types after runtime creation;
the independent oracle performs the same reads, mutation, teleport, hotbar
selection, and ticks directly through official obfuscated symbols. Two fresh
subject and two fresh oracle JVMs must agree with each other, across the
differential boundary, and with the frozen M3 signature.

`smokes/m4-durable-snapshot/` promotes the earlier in-memory checkpoint to a
durable canonical artifact. Two capture JVMs must write identical bytes; two
restore JVMs must reproduce the same internal fingerprint and direct
official-client tick-4 state. A separately corrupted artifact must fail its
embedded checksum. The artifact describes reconstruction and realized events;
it is not a serialized heap or a self-contained reproduction bundle.

`smokes/m5-reproduction-bundle/` packages that artifact with exact runtime
input declarations. Two pack processes must emit identical bytes; the CLI must
replay both the original and a copied path into the same state; a direct
official-client process supplies the behavioral oracle. Corrupt content and
validly encoded bundles naming the wrong client or toolchain must fail closed.

`smokes/m6-trace-explorer/` obtains fresh state traces through both the mapped
client and direct official-JAR paths. Equality is established before a single
field is changed in a copied trace. The CLI must identify the exact record,
label, field index, field name, and ordered values; malformed protocol input
must fail before analysis.

`smokes/m7-mod-loading/` consumes the already oracle-qualified client adapter,
then selects two different entrypoints solely from canonical JAR descriptors.
It proves code origin and deterministic glass/gold effects while separately
rejecting metadata mismatches, invalid descriptors, and a wrong Java subtype.

`smokes/m8-mod-version-diff/` packages two deterministic versions of one mod,
executes each twice beside a repeated no-mod baseline, records durable results,
and verifies exact baseline/version and version/version divergences plus result
corruption rejection.

`smokes/m9-scenario-minimization/` reuses the exact deterministic M8 artifacts
and opens fresh mod classloaders/runtimes for both versions on every candidate.
Two outer JVMs must produce the same one-minimal scenario, evaluation count,
provenance hashes, and exact divergence fingerprint.

`smokes/m10-native-render/` is a separate native evidence lane. It never places
the adapter's headless LWJGL substitutions on its classpath. Instead, it invokes
mapped and official Minecraft `Tessellator` bytecode through the pinned real
LWJGL JAR and Windows native library, draws into a Pbuffer, and compares complete
RGBA readbacks. This lane introduces no product-module or adapter dependency and
does not promote a rendering API.

## Adapter direction

Game-specific work will enter through new adapter modules, not through the API
or by placing implementation in the harness:

```text
scenario/driver -> kernel -> backend port <- retromcp/lwjgl adapter
                         |
                         +-> trace/oracle observers
```

An adapter may depend on the API, kernel, and semantic catalog. The API,
kernel, and catalog must never depend on an adapter. The reusable client adapter is an executable proof of
this direction. Replay-backed checkpoints, branch comparison, semantic GUI
control, and the narrow mod API remain adapter-side because their implementation
necessarily knows b1.7.3.

M11 applies the same rule to performance evidence. `FrameAttribution` in the
neutral analysis module accepts only named counters and times. The separate
`adapters/aero-model-lib` parser owns Aero's log vocabulary and maps it into
that neutral model. The native smoke counts submitted renderer operations
above the Pbuffer; neither the API nor kernel learns about Aero or OpenGL.

M12 keeps runtime control in a smoke-only Aero integration overlay. A Gradle
init script adds one mapped Mixin to the upstream test consumer; it controls
seed, chunks, camera, and duration on the game thread. Raw saves and frame logs
stay derived, while the existing analysis and minimization modules consume
only adapter-neutral frames and opaque record indices. No M12 behavior enters
the API, kernel, or maintained product graph.

M13 reuses that overlay but adds a fixed warmup and four explicit runtime modes.
`AeroDiagnostics` remains game-specific and exposes only the timing/counter
fields needed by the smoke. Persistence scanning stays inside the mapped test
hook; conclusions and invariant checks stay in the bounded smoke. No governor
or Aero behavior is promoted into Worldline product modules.

M14 adds frame-scoped queue, rebuild, invalidation, call, and return probes to
the same overlay. `AeroChunkProbe` owns their mapped vocabulary. The bounded
experiment reuses vanilla's priority sorter but exists only in the smoke
runtime; it is not an API, kernel feature, Aero patch, or approved mitigation.

M15 introduces a small adapter-runtime contract with explicit complete,
accepted/deferred, and stalled/deferred outcomes. A caller redirect is the only
place that maps those states back to vanilla's Boolean. Separate readiness and
Tessellator probes remain smoke-owned; neutral parsers consume their logs. This
qualifies the boundary without coupling product modules to mapped game types.

M16 adds a smoke-owned scheduler over that contract. It derives an accepted-work
limit from visible dirty debt, applies a rebuild-time envelope, and leaves the
vanilla priority sorter authoritative within the selected cohort. A separate
frame oracle freezes interpolation and game ticks, waits for global and visible
readiness, and hashes the complete RGBA framebuffer. The runner restores one
canonical save snapshot for both modes so world generation is outside the
differential boundary. None of this enters the API, kernel, or Aero checkout.

M18 keeps that Aero-checkout rule and only parameterizes the existing test-mod
save skip. A smoke-owned tick injects one non-forced world save so skip and
live twins can place `worldSaveMs` on the same Aero line as compile, GC, heap,
and allocation counters. The historical random spike stays a non-claim; no
scheduler or visual-threshold change enters product modules.

M19 extends only the smoke overlay: it can mark a bounded loaded-chunk set
dirty and replace vanilla's non-forced 24-chunk save constant with a positive
opt-in cap. Missing/zero configuration and every forced save retain vanilla
behavior. No save-budget API enters product modules or the Aero checkout.

## Compatibility boundary

Product modules currently compile with `--release 8`; tooling runs on JDK 21.
This keeps the host harness modern while avoiding accidental use of modern JDK
APIs in code that will eventually interact with the legacy game process.

## Controlled laboratory milestone

The baseline remains:

```text
bootHeadless()
loadWorld(...)
tick(1)

vanilla oracle: MATCH
```

The repository preserves that official-JAR differential proof through 16 ticks
and adds a second evidence layer:

```text
snapshot() -> restore/replay -> compare(hypothesis A, hypothesis B)
                         |
                         +-> semantic GUI actions
                         +-> isolated B173Mod JAR
```

Checkpoint restoration is reconstruction plus deterministic event replay, not
heap serialization. This makes the state contract explicit and fail-closed.
The mod context is intentionally small so an Aero-specific adapter can be
introduced later without exposing mapped Minecraft classes to mod code.
