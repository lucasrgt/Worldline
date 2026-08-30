# Worldline Architecture

This document defines the smallest architecture that can grow toward a
controlled Minecraft Beta 1.7.3 runtime without mixing the public driver,
runtime policy, and game-specific integration.

The canonical Gate, Runtime Fabric, receipt, pool, and cache flow is maintained
separately in [VERIFICATION_ARCHITECTURE.md](VERIFICATION_ARCHITECTURE.md).

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

external Java 8 spec -> worldline-testapi -> worldline-testmodel -> worldline-testkit -> provider SPI
                                                |                |
                                                v                v
                                      immutable results     b173 adapter
                                                |
                                                v
                                console / JSON / JUnit / agent
```

The modules are physical source roots and are compiled separately. The API is
compiled with no product classpath. The kernel is compiled with only the API on
its classpath. Reproduction depends only on the API; CLI depends only on API
and the stable product modules it exposes. Analysis depends only on trace.
This makes every declared dependency direction executable rather than
conventional.

### `testmodel`, `testapi`, and `testkit`

`worldline-testmodel` is the Java 8 neutral model boundary. It owns immutable
plans, contexts, results, status values, semantic divergence data, and reporter
events. `worldline-testapi` is the Java 8 authoring facade above it. It owns the
typed internal Java DSL (`test`/`it`, suites, hooks, expectations), fluent
per-test configuration, runtime-provider SPI, and the initial evidence-backed
selector catalog. Neither contains a runner, filesystem policy, Minecraft
classes, or mapped names.

`worldline-testkit` is Java 21 tooling above the neutral artifact modules. It
owns multi-spec discovery execution, selection, fresh-attempt isolation,
timeout and retry policy, external snapshots, artifacts, minimization, and
event-driven reporters. Every official runtime attempt holds a process-local
and cross-process exclusive lease. The executor remains sequential even when
a collected test requests concurrency; runtime concurrency fails closed.

The CLI discovers top-level specs or loads an explicitly named `WorldlineSpec`
from a bounded JAR or class directory. A separate bounded classpath exposes the
mod's own product classes without weakening the spec code-source check. The runtime-specific
`B173TestRuntimeProvider` stays in the mapped adapter and is discovered by
class name. Therefore an external mod compiles only against the packaged Java
8 authoring JAR (`api`, `testmodel`, and `testapi`), while the runner can use
modern Java without raising the mod's bytecode level.

### `optimization`

Owns only the source-retained `OptimizationRef` metadata annotation. It has no
dependencies and performs no runtime work. Modules opt into this compile-time
dependency only when they annotate an owned optimization site. A separate
harness check validates records and references; it never enables features or
transforms bytecode. External projects keep their records under
`worldline/optimizations/catalog/` in their own repositories. Worldline
evidence may cite an external stable ID, but the Worldline catalog must not
describe mod-specific algorithms or flags.

### `api`

Owns stable concepts visible to a scenario author. It must not expose RetroMCP,
LWJGL, mappings, instrumentation, or decompiled Minecraft types.

The Game UI Tree adds opt-in `UiMinecraftRuntime` and immutable
`GameUiNode` values. `GameUi` exposes the current screen, a snapshot of semantic
nodes, role/name lookup, inventory open/close, and clicks. Vanilla inventory
is promoted; Butter screens that implement `butter.testing.HostUi` are bound
reflectively. Other vanilla screens fail closed.

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
It owns the closed 25-category role contract, static role graph, fail-closed
lookup, and adapter manifests. The API owns immutable `SemanticMapping`
values, including optional official client aliases. Category files annotate
controlled b1.7.3 symbols already evidenced by Worldline; unknown or
duplicate symbols fail closed. Adapter manifests bind Worldline-owned sites
to those roles. External libraries such as Aero stay out of the catalog.
Worldline overlay sites for Aero live under `worldline/aero/` as a pinned
extension. Worldline does not depend on Aero types. Other mods adapt through
the extension SDK rather than new in-tree adapters.

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

`smokes/redstone-wire-power/` reuses that in-memory world with a standing
powered torch at `(8, 65, 8)` and one adjacent dust cell. Four ticks later
both the mapped runtime and the official server JAR must agree on dust
metadata and `isBlockIndirectlyGettingPowered` at the next cell.

`smokes/redstone-repeater-delay/` inserts a delay-1 idle repeater between the
torch and dust. Placement leaves the dust unpowered; six ticks later the
repeater is the active block, `isPoweringTo` is true on the output face, and
the official server JAR matches.

`smokes/redstone-repeater-delays/` runs delay-2/3/4 on three parallel lines.
`smokes/redstone-lever-button/` keeps a floor lever ON and pulses a side
button for 20 ticks. `smokes/redstone-piston-extend/` steps `updateEntities`
before `World.tick` so a torch-powered piston can finish extending. Repeater
locking is absent in b1.7.3; plates and BUD stay outside these fixtures.

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

M54 adds a strict `B173WindowTracker` beside the item coordinator. An explicit
chest activation expects one Packet100 descriptor decoded with Java modified
UTF, then correlates only the matching Packet104 window ID and exact 63-slot
shape. The immutable API pairs that descriptor with the combined view; window
close and transaction state remain outside this boundary.

M55 separates personal-window prediction into `B173PersonalWindowChannel` and
acknowledgement correlation into `B173PersonalTransactionTracker`. The inbound
inventory tracker retains the signed Packet103 cursor sentinel and preserves
window 0 independently of container Packet104 views. A staged immutable
left-click transition becomes visible only after the exact Packet106 true ACK
and does not require a Packet103 correction.

M56 extends the transaction tracker with rejected recovery phases. Packet106
false triggers the exact client Packet106 true re-enable ACK before further
reads. Packet104 and the cursor Packet103 are staged in order and atomically
replace inventory/cursor only after both validate; no partial correction leaks.

M57 keeps recipe knowledge outside the generic click API. A bounded personal
crafting composition shares the M55/M56 action counter and correlator, stages
multi-slot grid/result predictions, and commits each only on Packet106 true.
The package-private rejection probe supplies an authoritative post-craft audit.

M58 treats Packet101 as a request rather than an acknowledgement. The adapter
sends the tracked active ID only with an observed empty cursor, keeps the remote
window active locally, then issues an empty-slot no-op through the shared
personal transaction counter. Only Packet106 true on window 0 closes the local
lifecycle and publishes immutable `RemoteWindowClosure` evidence.

M59 adds a separate per-container action counter keyed by window-open epoch.
Before any active-window mutation, the 36-slot combined tail is reconciled by
item content with canonical personal slots 9-44. Each Packet106 true atomically
adopts the predicted 63-slot view, window-0 view, and cursor; partial commits are
not published. A clean server restart and fresh Packet104 are the persistence oracle.

M60 generalizes the same epoch-bound container channel to a typed furnace with
three owned slots and a 39-slot combined view. Packet103 updates reconcile
asynchronous furnace-owned changes while Packet105 is coalesced into bounded
cook, burn, fuel-duration, and completion-reset evidence. The exact sand/coal
workflow publishes only after the output, empty input/fuel, epoch, and progress
oracles agree.

M61 extends each adapter-private container step with an optional expected
statistic side effect. Furnace output action 5 can commit only after exact
Packet200 glass-stat evidence and its correlated Packet106 true; action 6 then
stores the cursor in the personal tail. The existing transaction tracker still
publishes the active view, window-0 view, and cursor as one logical transition.

M62 separates a remote window's wire-declared container count from its player
tail offset. Chest and furnace retain offsets 27 and 3; workbench Packet100
declares its nine matrix slots while the result slot makes the combined player
tail begin at 10. Packet104 shape checks, tail reconciliation, Packet103 reverse
mapping, and close safety all consume that single derived layout.

M63 adds a button field to adapter-private container steps and uses it only in
one bounded workbench workflow. A left-take followed by three right placements
commits the active view, canonical personal view, and cursor after each ACK.
SlotCrafting results remain explicitly modeled until the next output click can
compare the final slabs prediction with the official server.

M64 extracts that workflow into a dedicated epoch-bound workbench channel.
Output action 5 requires the exact slabs return and crafted-stat side effect
before atomically clearing result/matrix; action 6 stores into both combined and
canonical personal views. Empty owned state then composes with the existing
safe-close proof and restart oracle.

M65 keeps the personal-window action counter as the single owner of armor
Packet102 transitions. A bounded peer equipment tracker correlates Packet5
entity IDs with previously bound usernames and preserves the legacy reversed
mapping between window slots 5..8 and equipment slots 4..1. Restart evidence
pairs the actor's count-bearing Packet104 view with the peer's count-free
Packet5 state.

M66 adds a dedicated combat channel and tracker while retaining the shared
Packet20 identity authority. Outbound Packet7 accepts only a username and uses
the connection's local entity ID plus the resolved target ID. Attacker and
victim streams remain distinct evidence: fresh target Packet38 on one side,
ordered local Packet38 then Packet8 on the other.

M67 reuses the active-container transaction sequencer in the reverse direction.
The chest source is adapter-owned, the destination is mapped through the single-
chest player tail, and matching Packet106 commits the active view, canonical
personal view, and cursor together. Restart evidence keeps persistence outside
the public value contract.

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

Game-specific runtime work will enter through driver modules, not through the
API, the harness, or a new in-tree adapter per mod:

```text
scenario/driver -> kernel -> backend port <- retromcp/lwjgl adapter
                         |
                         +-> trace/oracle observers
```

An adapter may depend on the API, kernel, and semantic catalog. The API,
kernel, and catalog must never depend on an adapter. Runtime drivers are the
closed `b173-client`, `b173-server`, `modloader-forge`, and `stationapi` set.
StationAPI is a process-isolated TestKit driver whose qualified surface grows
only through official-runtime milestones. `modloader-forge` is a profiler-only
source-injection driver with an independent, hash-pinned ModLoader/Forge boot,
capture, and clean-shutdown qualifier. That narrow runtime proof does not imply
a generic legacy TestKit provider or performance equivalence between loaders.
Mods publish `worldline/extensions/` manifests in their own repositories. Worldline pins
`aero-model-lib` only as an overlay for oracled smokes. See
`docs/EXTENSION_SDK.md`. The reusable client adapter is an executable proof of
this direction. Replay-backed checkpoints, branch comparison, semantic GUI
control, and the narrow mod API remain adapter-side because their implementation
necessarily knows b1.7.3.

M11 applies the same rule to performance evidence. `FrameAttribution` in the
neutral analysis module accepts only named counters and times. The separate
`adapters/aero-model-lib` parser owns Aero's log vocabulary and maps it into
that neutral model. The native smoke counts submitted renderer operations
above the Pbuffer; neither the API nor kernel learns about Aero or OpenGL.

M12 keeps runtime control in a smoke-only Aero integration overlay. A Gradle
init script adds Worldline mixins from `worldline.aero.mixin` to the upstream
test consumer; they control seed, chunks, camera, and duration on the game
thread. Raw saves and frame logs
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

M68 adds no product API. A test-only Gradle overlay drives the pinned real
StationAPI/Aero client through vanilla `ConnectScreen`, records Packet1/13/51
handler boundaries, and correlates subsequent renderer frames with Aero logs.
All generated worlds, logs, and official artifacts remain outside the public tree.

M69 keeps Packet18 in the modular combat boundary. The outbound channel emits
one exact swing request, while the inbound tracker resolves the observed entity
through the shared Packet20 identity registry. The typed API deliberately keeps
request and observation separate because Packet18 carries no target or damage.

M70 adds no public API. Its test orchestration and runtime mixins let a wire fixture own the
official server and M66/M69 actors; a real Aero observer independently binds
Packet20 identities, applies Packet18 then Packet38 at handler TAIL, and counts
subsequent renderer frames and log rows. These clocks remain evidence streams,
not a causal performance model. The adapter also retires terminal dropped-item
IDs when the official server reuses them, while live duplicate IDs still fail.

M71 adds no public API or adapter behavior. A separate test overlay preserves
M70 and reconstructs fresh matched control/event arms around an exact Packet3
handler anchor. Its probe uses only network-handler and renderer TAIL markers;
it never reads the asynchronously buffered Aero file in-process. The runner
segments logger stdout after exit, excludes WorldFlush, strictly parses selected
frame rows, confirms them against the normally flushed file, and emits dynamic
descriptive per-arm and per-pair summaries without an inferential verdict.

M72 also adds no public API or adapter behavior. Its smoke-owned universal mod
keeps common/server classes free of Aero and client imports, while a client-only
entrypoint owns the Aero renderer. Separate StationAPI server/client worktrees
load the same content definition; an explicit coordinate-and-nonce message
bridges the plain block entity because generic NBT update synchronization is not
claimed. Generated runtimes and content artifacts remain outside the public tree.

M73 likewise adds no public API or product adapter behavior. A separate
smoke-owned content mod extends the explicit-message pattern to a fixed
sixteen-cell tracker, an identical client activation message, and a tracked-plan
readiness acknowledgement in absent/present arms. The server sends every
present-cell state through an explicit content message after that acknowledgement.
The runner owns fresh StationAPI server/client worktrees,
balanced order, async log segmentation, strict structural treatment checks, and
descriptive summaries. The server closure remains free of Aero/client/LWJGL
imports.

M74 adds no public API or product adapter behavior. Its isolated smoke reuses
the tracked paired content protocol but disables the selective Aero logger and
records complete renderer HEAD-to-HEAD intervals in preallocated primitive
arrays. A test-only invoker resets pinned at-rest counters independently of
logger/Mixin order. Binary serialization and typed parsing occur only after the
measurement bracket seals; generated artifacts stay outside the public tree.

M75 adds no public API or product adapter behavior and leaves M74 frozen. Its
overlay compiles the M74 fixture unchanged, then redirects only the client-side
`queueAtRest` invocation for a nested level. Server content, synchronization,
camera, renderer dispatch, and census remain constant. A separate runner owns
fresh mirrored ladders, exact per-record treatment validation, and descriptive
level summaries; all artifacts remain ignored evidence.

M76 adds no public API or product adapter behavior and leaves M74/M75 frozen.
Its client overlay removes one exact renderer mapping or redirects the nested
Aero call while retaining the same synchronized sixteen-cell fixture. Treatment
setup is excluded before the retained baseline; vanilla max framerate and Aero
pacer-off state are runtime gates. A separate runner owns fresh mirrored
triplets, exact per-record `0/0`, `16/0`, or `16/16` validation, and descriptive
stage deltas. Mixed timing is evidence, not stage-cost attribution.

M77 adds no public API or product adapter behavior and leaves M74-M76 frozen.
Its client-only overlay wraps one Worldline renderer method plus Aero's
`queueAtRest` direct-fallback and empty-flush methods with primitive synchronous
timers. A separate fixed-capacity
sidecar is index-aligned to the M74 census and written only after seal. The
server closure remains free of Aero/client imports; all generated binaries and
timing evidence remain ignored local artifacts. Timings are instrumented
descriptions, not paged, additive, causal, or uninstrumented stage costs.

M78 adds no public API or product adapter behavior. Its client-only Mixin adds
the Aero page marker to the remote M74 block-entity class at runtime; the
compiled common/server class remains Aero-free. A client recorder binds direct
renderer/enqueue/flush spans and public page counters to the M74 record index,
then writes a distinct binary artifact post-seal. The qualified bracket is
warmed and rebuild-free; cold cache construction remains outside the claim.

M79 adds no public API or product adapter behavior and leaves M74-M78 frozen.
A client-only model accessor calls pinned Aero's public disposal boundary once
after 300 retained records. The event recorder keeps a fixed primitive state
and writes a 68-byte artifact after seal; M78's existing page sidecar carries
the corresponding rebuild record. The server/common closure remains Aero-free.
This qualifies an explicit cold transition, not a natural invalidation policy
or an uninstrumented/causal cost.

M80 adds no public API or product adapter behavior and leaves M74-M79 frozen.
A smoke-owned common message boundary asks the Aero-free server to validate and
remove one exact synchronized cell. Client primitive state binds the exact ACK,
air block, first fifteen-member renderer record, and one natural page rebuild;
a 36-byte sidecar is written only after seal. A stale legacy BE lookup is not
treated as live renderer membership and remains outside the claim.

M81 adds no public API or product adapter behavior and leaves M74-M80 frozen.
Its smoke-owned message boundary validates and removes two exact synchronized
cells positioned on opposite sides of a natural page boundary. Client state
binds one ACK, two air blocks, the first fourteen-member record, and exactly
two page rebuilds into a 40-byte post-seal sidecar. The common/server closure
remains free of Aero and client imports; timings remain descriptive.

M82 adds no public API or product adapter behavior and leaves M74-M81 frozen.
A server-safe message handler validates three exact target-set variants while
the client-only overlay records one reduced-membership transition per fresh
arm. Fixed 44-byte sidecars bind cardinality and expected rebuild count to the
M74/M78 artifacts. A smoke-local camera normalization removes legacy input
quantization before the inherited strict pose gate; no product code changes.

M83 adds no public API or product adapter behavior and leaves M74-M82 frozen.
Its common server-safe handler accepts only two exact topology codes and two
exact target sets. The client-only overlay shares M82's camera normalization,
requires ACK plus both air blocks, and binds one-versus-two rebuild records to
fixed 44-byte post-seal artifacts. The server closure remains Aero-free.

M84 adds no public API or product adapter behavior and leaves M74-M83 frozen.
Its server-safe handler accepts two exact three-member topologies over a
four-page-key scene. The client-only overlay preserves three cached batch pages
plus one singleton direct fallback in both arms, requires ACK and three air
blocks, and binds one-versus-three rebuild records to distinct 44-byte
post-seal artifacts. The common/server closure remains Aero-free.

M85 adds no public API or product adapter behavior and leaves M74-M84 frozen.
Its server-safe two-phase handler validates one exact removal and restoration
at the same coordinate. A distinct restore-state message buffers the original
derived nonce until the flattened block update is visible client-side. The
client overlay binds `16 -> 15 -> 16` membership, cached-page/direct-fallback
transitions, and the single restoration rebuild to a fixed 52-byte post-seal
artifact. The common/server closure remains Aero-free.

M86 adds no public API or product adapter behavior and leaves M74-M85 frozen.
Its common protocol adds an exact generation and operation to same-cell
remove/restore requests, ACKs, and restore-state delivery. Primitive client
state rejects duplicate, skipped, reordered, cross-generation, coordinate, and
nonce drift. A fixed 60-byte post-seal sidecar binds four request/event pairs;
complete M74/M78 records remain authoritative for topology and rebuilds. The
common/server closure remains Aero-free.

M87 adds no public API or product adapter behavior and leaves M74-M86 frozen.
Its generation protocol additionally binds exact cell indices zero and one.
The 76-byte sidecar records four request/event/index triples; complete M74/M78
records prove fallback recovery for index zero and immediate rebuild recovery
for index one. Mutations remain sequential and the server closure Aero-free.

M88 adds no public API or product adapter behavior and leaves M74-M87 frozen.
It reverses only the two generation-bound cell identities. The same 76-byte
sidecar and complete records prove that index one still rebuilds immediately
when first and index zero still uses direct fallback when second. This is an
exact-fixture distinction, not a general positional causality claim.

M89 adds no public API or product adapter behavior and leaves M74-M88 frozen.
It targets exact index four at `(x,y,z+1)`, the other identity in index zero's
pinned natural two-member page. The 52-byte sidecar and complete records prove
the same fallback during removal and one rebuild at restoration. The result is
fixture-specific; common/server code remains Aero-free.

M90 adds no public API or product adapter behavior and leaves M74-M89 frozen.
It targets exact index two at `(x,y+2,z)`, a sibling in index one's pinned
six-member page. The 52-byte sidecar and complete records prove fully batched
rebuilds at both removal and restoration, with zero direct fallback. The result
is fixture-specific; common/server code remains Aero-free.

M91 adds no public API or product adapter behavior and leaves M74-M90 frozen.
It removes indices one and two from their exact pinned six-member page, then
restores them in reverse. Ordinal/operation/index-bound messages and the
76-byte sidecar bind all four transitions; complete records prove four page
calls, zero direct fallback, and one rebuild throughout. Common/server code
remains Aero-free.

M92 adds no public API or product adapter behavior and leaves M74-M91 frozen.
It additionally removes exact index three, reaching three remaining members,
then restores indices three/two/one. The 100-byte sidecar binds six transitions;
complete records prove four page calls, zero direct fallback, and one rebuild
throughout. Common/server code remains Aero-free.

M93 adds no public API or product adapter behavior and leaves M74-M92 frozen.
It removes the exact six-member page at indices one/two/three/five/six/seven,
then restores it in reverse. A 172-byte sidecar binds twelve transitions.
Complete records distinguish batched counts two through six, the direct
one-member route, and the empty-page route. Page TTL is fixed at 100000 frames
to isolate cardinality; default-TTL eviction remains outside the claim.
Common/server code remains Aero-free.

M94 adds no public API or product adapter behavior and leaves M74-M93 frozen.
It reuses M93's exact page but leaves the pinned normal TTL properties absent.
The client waits for the 600-frame default sweeper to retire only the empty
target cache entry, then restores one member directly and recompiles at two.
The 184-byte sidecar binds the expiry record/counters and twelve transitions.
High-memory, explicit-TTL, and max-cache eviction paths remain outside scope;
common/server code remains Aero-free.

M95 adds no public API or product adapter behavior and leaves M74-M94 frozen.
It reuses M78's exact four-page scene under a three-entry client cache, with a
long TTL that excludes expiry. A client-only overlay records cumulative
capacity evictions beside the existing page state and timing spans. Complete
records require one to four rebuilds with an equal capacity-eviction delta,
three resident pages, and zero direct fallback. Equal `lastUsedFrame` ties
follow the pinned `HashMap` iteration order. Common/server code remains Aero-free.

M96 adds no public API or product adapter behavior and leaves M74-M95 frozen.
It runs the same four page keys under a two-entry cache. Because equally recent
cache entries are traversed through pinned `HashMap` order, fresh JVMs may
settle into rebuild mode three or four. The client-only parser accepts only
those values and couples every cumulative capacity-eviction delta to the same
record's rebuild count. Common/server code remains Aero-free.

M97 adds no public API or product adapter behavior and leaves M74-M96 frozen.
It sets the same four-page fixture's client cache to one entry. The client-only
record gate requires all four pages to rebuild and the cumulative eviction
counter to advance by four on every retained record, with no direct fallback.
This removes M96's equal-age tie mode for the exact fixture. Common/server code
remains Aero-free.

M98 adds no public API or product adapter behavior and leaves M74-M97 frozen.
It passes literal cache maximum zero into the client runtime while retaining
M97's expected observed cache size one. The pinned eviction loop cannot remove
its protected freshly compiled key when no other victim exists, so each later
compile displaces the prior page and preserves a one-page floor. Common/server
code remains Aero-free.

M99 adds no public API or product adapter behavior and leaves M74-M98 frozen.
It restores a one-entry cache but lowers the per-frame rebuild budget to two.
For the exact sorted four-page fixture, two pages rebuild, the other two keys'
four instances use direct rendering, and the eviction counter advances by two
in every retained record. Common/server code remains Aero-free.

M100 adds no public API or product adapter behavior and leaves M74-M99 frozen.
It lowers the same cache1 fixture to one rebuild per frame. The retained key
causes exact alternation between a two-page-call/four-direct record and a
one-page-call/ten-direct record; rebuild and eviction deltas remain one.
Common/server code remains Aero-free.

M101 adds no public API or product adapter behavior and leaves M74-M100 frozen.
It lowers the same cache1 fixture to zero rebuilds per frame. No page can enter
the cache, so every retained record uses the direct path for all sixteen
synchronized instances, with zero rebuilds and evictions. Common/server code
remains Aero-free.

M102 adds no public API or product adapter behavior and leaves M74-M101 frozen.
It passes literal negative one to the same cache1 fixture; the pinned negative
predicate removes the rebuild ceiling, so all four page keys compile and evict
while direct fallback remains zero. Common/server code remains Aero-free.

M103 adds no public API or product adapter behavior and leaves M74-M102 frozen.
It disables cell pages and adds client-only observation of the exact immediate
direct overload. This cross-checks sixteen direct calls against M74 renders
even though subsequent empty flushes reset the public cell counters to zero.
Common/server code remains Aero-free.

M104 adds no public API or product adapter behavior and leaves M74-M103 frozen.
It uses one unified client-only sidecar to compare pages enabled and disabled
in two balanced same-plan pairs. Automatic plans now include the camera support
column so the unchanged strict pose gate cannot race gravity over depressions.
Common/server code remains Aero-free.

M105 adds no public API or product adapter behavior and leaves M74-M104 frozen.
It pairs capacity one with the pinned negative-one unlimited cache sentinel
under one recorder and same-plan scene. The smoke server now fixes one vanilla
stone support block outside the sixteen Aero cells before teleport, making
replayed camera plans independent of gravity timing. Common/server code still
has no Aero dependency.

M106 adds no public API or product adapter behavior and leaves M74-M105 frozen.
It pairs literal minimum page populations two and five with unlimited cache and
rebuild budget. The higher threshold keeps two natural pages cached and routes
four instances directly; the lower threshold keeps all four pages cached. The
test client fixes yaw/pitch before readiness to remove physical mouse drift,
while X/Y/Z remain server-authored and strictly validated. Common/server code
remains Aero-free.

M107 adds no public API or product adapter behavior and leaves M74-M106 frozen.
It pairs the pinned skip-individual switch under minimum2. A client-only mixin
implements the managed page-renderable contract and credits exact identities
only after server nonce reconciliation; the control reaches the same four
cached pages through the registered renderer. Common/server code remains
Aero-free.

M108 adds no public API or product adapter behavior and leaves M74-M107 frozen.
It pairs pinned cell sizes two and eight under minimum2 and skip-individual
false. A fixed aligned test plan changes only the page partition: four
size-two pages versus one size-eight page, with the same sixteen identities and
individual renderer calls. Common/server code remains Aero-free.

M109 adds no public API or product adapter behavior and leaves M74-M108 frozen.
It pairs raw cell-size zero with explicit one under the same plan and proves
the pinned lower clamp publishes effective size one. Both arms exercise
sixteen distinct below-minimum cells and the identical direct-fallback path.
Common/server code remains Aero-free.

M110 adds no public API or product adapter behavior and leaves M74-M109 frozen.
It pairs raw cell-size thirty-three with explicit thirty-two and proves the
pinned upper clamp publishes effective size thirty-two. Both arms group all
sixteen identities into the same one-page path. Common/server code remains
Aero-free.

M111 adds no public API or adapter mutation. It reuses the bounded protocol-14
chunk cache against two fresh unmodified official servers and addresses
absolute chunk `(0,0)`, separating deterministic terrain from Beta 1.7.3's
variable initial player spawn. The semantic digest covers the non-air count
and derived top-Y/ID/metadata surface profile; the complete block-ID plane is
diagnostic because buried population order can vary. Complete metadata and
lighting planes remain separate boundaries. M71-M110 are frozen while the
active architecture roadmap returns to vanilla systems.

M112 adds no public API. It consumes the two existing Packet51 nibble planes
from the same absolute chunk and freezes their complete ordered hashes and
sixteen-bin histograms across fresh official worlds. This establishes
deterministic light-state input without claiming a light-engine cause, update,
renderer effect or cross-chunk rule. A new adapter-side NBT seed places only
the pre-login player pose at the target chunk; world blocks remain
server-generated and the existing 256-chunk cache bound stays unchanged.

M113 adds no public API. It extends the adapter-side player seed with one
optional exact hotbar block stack, then composes already-qualified Packet15,
Packet53, heartbeat and Packet51 boundaries. Lighting evidence comes only from
a fresh full-chunk send after the unmodified official server accepts and
settles the glowstone placement; the incremental cache is never treated as a
client light engine. The ordered delta remains a smoke oracle rather than a
generic lighting implementation.

M114 adds no public API. It composes fixed-seed terrain, Packet14 mining,
Packet53 cache replacement, bounded heartbeats and a fresh Packet51 to observe
one official scheduled-water transition. The product adapter still contains
no fluid simulation. A near-source official player-NBT pose keeps the smoke
inside its fluid boundary. The inbound combat tracker now records any valid
status-ordered local health decrease rather than treating all hurt as M66's
`20 -> 18`; the existing expected-health boundary remains fail-closed. M66's
canonical fixture now uses exact NBT-seeded equipment and bounded position
heartbeats instead of drop pickup and uncontrolled falling.

M115 adds the first public post-placement interaction surface through
`BlockActivationMultiplayerSession`. The b1.7.3 adapter reuses its strict
empty-hand Packet15 encoder but exposes it without arming a container-window
expectation. The lever fixture separates construction from treatment with a
200-tick stabilization boundary, and only Packet53 plus a fresh Packet51 can
publish the state transition. No redstone evaluator exists in Worldline.

M116 extends that same neutral surface with selected held-item use, allowing
official item behavior rather than only ItemBlock placement. The b1.7.3 path
accepts positive signed-short item IDs but still derives the entire stack from
window zero and refuses active-container/cursor drift. Redstone simulation
remains wholly server-side: Worldline observes dust placement, lever state and
wire strength only through Packet53 and a fresh Packet51.

M117 adds no public API. It composes the same activation twice around a frozen
powered precondition and observes the official wire returning from strength 15
to zero. The complete-chunk comparison admits only the lever and wire cells,
and a fresh Packet51 must retain both off states. Worldline still implements no
redstone evaluator, scheduler or topology model.

M118 also adds no public API. Item 330 exercises the already-published
held-item boundary to create both halves of an official iron door, while the
lever activation boundary supplies its power transition. Worldline records the
lever and both door metadata changes only after Packet53 and a fresh Packet51;
consumer logic remains entirely inside the official server.

M119 adds no public API or physics implementation. It composes official sand
placement, Packet14 support removal, Packet53 cache replacement, bounded
heartbeats and a fresh Packet51. The complete-chunk oracle proves only the
source/destination states of one settled fall; the transient entity remains an
official-server mechanism rather than a Worldline simulation.

M120 adds no public API or fluid evaluator. Its constructor session builds a
bounded source/gate trench; separate treatment and verifier sessions provide
fresh Packet51 snapshots on both sides of the mutation. This avoids comparing
incremental water cache state against a reload and confines the full-chunk
delta to the sole legal horizontal destination.

M121 adds no public API or world generator. It composes the existing complete
chunk cache across a fixed 3x3 coordinate region, then repeats the census after
official save and restart. Aggregate solid count, exact surfaces and solid seam
pairs remain authoritative. Interior position masks, IDs and metadata are
retained as diagnostic evidence because equal heartbeat counts do not make
official scheduled/random-tick outcomes deterministic across fresh process
replicas.

M122 adds no public API or light engine. It traverses the already-published
block-light and sky-light accessors across M121's nine complete chunks after
the same save/restart lifecycle. The two planes are hashed independently with
no normalization and retain full sixteen-value histograms. All lighting
generation and propagation remains authoritative inside the official server.

M123 adds no public API or cross-chunk evaluator. It places a qualified held
block at the final X coordinate of one chunk, then compares fresh complete
light planes from that chunk and its east neighbor. The exact water attenuation
and both ordered delta sets remain official-server observations; Worldline only
correlates the intervention and immutable snapshots.

M124 adds no public API. A witness captures M123's lit precondition while the
actor remains connected; source removal and a final reader then produce exact
decrease and residual comparisons for both chunks. The empty residual deltas
qualify recovery without introducing a Worldline scheduler or light engine.

M125 adds no public API or fluid evaluator. A constructor builds the same
bounded source/gate topology as M120 but places source and destination on
opposite sides of a chunk seam. Separate treatment and reader sessions prove
the official scheduler mutates only the neighboring chunk target.

M126 adds no public API or redstone evaluator. It places the lever block in the
east chunk and its attached support and wire in the west chunk, then partitions
the fresh post-activation delta by chunk. Power computation remains entirely
inside the official server.

M127 adds no public API. It preserves M126's original off snapshots, qualifies
the powered precondition with a fresh witness, and compares a separate final
reader against both powered and original states. The exact inverse deltas and
empty residuals remain immutable observations of the official server.

M128 adds no public API or consumer model. It reuses the edge-column placement
boundary but places an official two-block iron door above the west support and
its lever on the east face. Separate complete snapshots retain the official
server's exact two-door-plus-one-lever state partition.

M129 adds no public API. It qualifies M128's open state through a fresh witness,
then compares an independent final reader against both open and original closed
snapshots. Exact inverse deltas and empty residuals remain server-authored
observations rather than a Worldline consumer simulation.

M130 adds an opt-in Nether flag to the dedicated-server adapter while all prior
constructors retain `allow-nether=false`. Server-property composition moves to
a bounded helper, and player seeding admits only vanilla dimensions `0` and
`-1`. The official server remains authoritative for world selection, terrain
generation, protocol traffic and persisted dimension state.

The v1.118.1 oracle hardening keeps exact netherrack/bedrock positions while
normalizing lava and mushroom cells whose scheduled/decorative state varied
before the first client snapshot.

M131 adds `DimensionSession` without removing any earlier multiplayer surface.
The wire client validates Packet1's dimension byte, while inbound Packet9
updates the same typed state. Only an actual `0↔-1` change clears the bounded
remote chunk cache; a same-dimension respawn does not discard valid data.

M132 adds no public API or adapter behavior. It composes the existing inventory,
placement, held-item use and remote-world boundaries into a complete official
portal fixture. Its oracle binds fourteen stable obsidian cells to six exact
air-to-portal transitions and a fresh-session persistence observation.

M133 adds no public API. It composes M132's constructed portal with M131's typed
Packet9 lifecycle: the same connection moves into the portal, changes dimension,
accepts the corrected destination pose and repopulates its cleared chunk cache
with decoded Nether data. Exact destination search coordinates stay outside the
frozen contract.

M134 keeps the same connection through a second dimension lifecycle. A bounded
portal-plane detector handles either vanilla orientation and negative world
coordinates, the player exits for cooldown and Packet9 returns it to dimension
`0`. Each dimension switch clears and repopulates the remote cache independently.

M135 adds `RespawnSession` without weakening the cumulative multiplayer API.
The signed Packet8 tracker preserves nonpositive overkill values; only the
typed respawn result normalizes that dead state to zero. A production Packet9
encoder and a monotonic inbound respawn epoch distinguish a real same-dimension
response from the cached dimension value, while the server remains authoritative
for the corrected spawn pose, chunk contents and saved player state.

M136 makes the respawn evidence dimension-aware. `RemoteRespawn` retains both
the request-side and response-side dimension, while the original constructor
still represents equal source/destination dimensions. The adapter always
expects vanilla's death destination `0`; a `-1→0` response therefore triggers
the same cache reset already qualified for portal travel, followed by an
independent Overworld chunk and persistence oracle.

M137 adds `ExplosionSession` and immutable `RemoteExplosion` evidence. Packet60
is intercepted before the generic skipper, decoded with the exact protocol-14
payload and atomically projected into the same remote chunk cache used by
Packet52/53. The Beta packet carries no later-version motion vector. Blast
randomness remains server-authored; Worldline freezes structural containment
and persisted state rather than a variable ray result.

M138 adds no public API. It reuses the accepted block placement, dig, scheduled
heartbeat, Packet53 cache update and fresh Packet51 persistence boundaries.
Because the lava schedule permits unrelated world random ticks during the same
window, the immutable delta oracle is explicitly limited to the declared
source and target cells instead of overclaiming whole-chunk causality.

M139 also adds no public API. It composes accepted block placement with the
same Packet53 cache and fresh Packet51 persistence boundaries to qualify one
official material rule: still water beside still lava converts the lava source
to obsidian. The two-cell causal hash excludes unrelated scheduled and random
world changes.

M140 adds no public API. It composes generic held-item Packet15 use with the
incremental world cache and fresh Packet51 persistence oracle. The deterministic
sapling-root transition is hashed, while the randomized vanilla oak geometry is
validated structurally rather than frozen as a universal layout.

M141 adds `MobObservationSession` and immutable `RemoteMobSpawn` evidence above
the cumulative session hierarchy. `B173MobTracker` consumes Packet24 and every
protocol-14 metadata field type into a bounded queue; `B173PlayInbound` owns the
single reader and exposes typed waits. The first qualified producer is an
official default pig spawner observed identically by two peers. Movement and AI
remain separate downstream boundaries rather than inferred from creation.

M142 adds no public API or adapter behavior. It composes the existing held-block
placement, lever activation, heartbeat, incremental Packet53 cache and fresh
Packet51 persistence boundaries around the official piston block event. The
oracle freezes only the raised fixture volume; generated water below the tower
is outside the causal attribution boundary.

M143 adds no public API. It introduces an independent fresh-session precondition
between extension and retraction, then reuses the same server-authored cache and
persistence boundaries. Normal-piston stone retention is an observed invariant,
not generalized to sticky pistons or other payloads.

M144 adds no public API or adapter behavior. It changes only the fixture's
server item/block from normal piston `33` to sticky piston `29`, preserving the
same geometry and lifecycle. The resulting stone pullback is therefore a
server-authored contrast rather than a new client-side model.

M145 adds no public API. It preserves the M142 normal-piston treatment while
expanding the payload to two distinct block states. The distinct middle and
terminal transitions keep chain displacement observable through the existing
Packet53/Packet51 world cache instead of inventing per-block identities.

M146 adds no public API or adapter behavior. It replaces M142's movable stone
with official obsidian `49:0` while preserving the piston, lever, destination
and fresh-session lifecycle. The exact negative transition is server-authored:
only the powered lever changes in the raised fixture, while piston, payload and
destination remain invariant.

M147 adds no public API or adapter behavior. It composes four fresh official
server runs into a matched capacity boundary: an alternating twelve-block chain
is fully displaced, while the same geometry with thirteen blocks is fully
rejected. Fresh Packet51 snapshots remain the authority for the complete chain;
the live cache is not promoted beyond its already qualified control states.

M148 extends `MobObservationSession` with immutable, fixed-point
`RemoteMobMovement`. `B173MobTracker` strictly consumes Packet30-34, maintains
per-entity quantized state and only queues movement for an explicitly observed
Packet24 identity. The dedicated-server adapter retains its old defaults and
adds a narrow animal-enabled factory for this official AI fixture.

M149 extends `MobObservationSession` with `attackMob` and immutable
`RemoteMobDeath`. `B173MobTracker` completes a death only after Packet38 status
3 and Packet29 destroy for an explicitly observed Packet24 identity, recording
whether status 2 arrived first. Packet7 is the official diamond-sword cause;
randomized porkchop drops stay outside the frozen hash.

M150 extracts Packet38/29 to `B173EntityEvents` and mob waits to
`B173PlayWaits`. The last Packet24 identity is adapter-owned: observed
movement, Packet7 and death no longer accept a caller entity id. The pork
oracle is the first shared Packet21 stack `319:1:0` after a bounded retry.

M151 adds no public API. It composes held-item Packet15 flint and steel with
the Packet53 cache and a fresh Packet51 login to prove netherrack fire `51:0`
is not a random-tick extinguish candidate.

M152 also adds no public API. It places wool `35` beside the M151 flame and
uses a fresh-login Packet51 snapshot as the consumption oracle, because the
live Packet53 window may miss the random-tick consume.

M153 adds `RemoteDispenserLoad` and a Trap-window open/load path. Packet100
type 3 maps to a 9-slot dispenser plus the personal tail. A side lever ejects
the loaded stack as Packet21.

M154 adds `RemoteObjectSpawn` and `ObjectObservationSession`. Official Packet23
is 21 bytes plus six velocity shorts when thrower is positive. Selected-item
air use is Packet15 at `(-1,255,-1)` direction 255. Type 1 is a boat.

M155 reuses that Packet23 tracker. Minecart item `328` on rail `66` emits type
`10` with thrower 0 and a quantized pose at the rail center.

M156 adds no public API. Wooden hoe Packet15 turns dirt into farmland `60:0`,
and a still-water neighbor hydrates at least one plot to `60:7` that survives
restart.

M157 reuses air-use Packet15 and the Packet23 tracker. A bow with arrows emits
type `60` whose thrower is the shooter entity id.

M158 adds `RemoteBedUse` and a Packet17/70 tracker. Daytime bed use is chat
refusal; night occupy is Packet17 at the head cell. SMP with monsters off
skips night and clears the occupied bit. Packet70 reason 0 is not this gate.

M159 adds no public API. Reed item `338` on dirt beside still water grows
from `83` height 1 to height 2+ by official random ticks and persists.

M160 adds a `B173PlayerSeed.writeInventory` health overload. Empty-hand
Packet15 on BlockCake `92:0` becomes `92:1` and Packet8 heals 3 hearts.

M161 reuses air-use Packet15 and the Packet23 tracker. Snowball item `332`
emits type `61` with thrower 0 near the actor.

M162 adds no public API. Wooden door item `324` places BlockDoor halves
`64:0`/`64:8`; empty-hand Packet15 toggles open `64:4`/`64:12` and back.

M163 also adds no public API. Trapdoor item `96` on an east face is `96:3`;
empty-hand use toggles `96:7` and back. The closed state persists.

M164 adds no public API. Stone pressure plate `70:0` powers to `70:1` when the
headless actor steps on it and depowers when they step off.

M165 adds no public API. Stone button `77:1` powers to `77:9` on empty-hand
use and depowers after the official 20-tick delay.

M166 adds `RemoteNoteEvent` and a Packet54/61 decoder. The first click of a
stone-supported note block is Packet54 instrument 1 pitch 1. Pitch lives in
the tile entity, so block metadata stays `25:0`.

M167 adds no public API. Cactus item `81` on sand `12` remains `81:0` after a
live hold and a fresh login.

M168 adds no public API. Empty bucket `325` on still water `9:0` produces air
and water bucket `326:1:0` that persist across restart.

M169 reuses air-use Packet15 and the Packet23 tracker. Egg item `344` emits
type `62` with thrower 0.

M170 adds no public API. Repeater item `356` places unpowered `93:3`; a side
lever produces powered `94:3` after one tick and returns to `93:3`.

M171 adds no public API. Pumpkin item `86` with look yaw `-90` places `86:1`
and that facing persists.

M172 adds no public API. Wooden pressure plate `72:0` powers to `72:1` when
the headless actor steps on it and depowers when they step off.

M173 adds no public API. Two adjacent fence items `85` place as `85:0` and
persist.

M174 adds no public API. Ladder item `65` on an east face is `65:5` and
persists.

M175 adds no public API. Floor torch item `50` on stone is `50:5` and
persists.

M176 adds `RemoteSignText` and a Packet130 tracker. Standing sign `63`
accepts four UCS-2 lines that survive a fresh login.

M177 adds `RemotePaintingSpawn` and a Packet25 tracker. Two peers correlate
the same painting entity, pose and direction. Title is observed, not hashed.

M178 adds `B173NoteAccess.poll`. Jukebox item `84` places `84:0`. Gold disc
`2256` on that cell emits Packet61 effect `1005` data `2256`, empties the
selected slot, and writes metadata `84:1` that persists.

M179 adds no public API. Wooden hoe `290` tills dirt; seeds `295` plant wheat
`59:0` on hydrated farmland `60:7`. The crop persists at metadata zero.

M180 reuses `awaitObjectSpawn(90)`. Fishing rod `346` in air emits Packet23
type `90` with thrower `0`; two peers share the hook identity and pose.

M181 adds no public API. Empty bucket `325` picks up still lava `11:0` into
`327:1:0` and leaves the source cell as air.

M182 adds no public API. Floor redstone torch item `76` on stone is `76:5`
and persists.

M183 adds no public API. Rail item `66` with look yaw `0` places `66:0` and
persists.

M184 adds no public API. Powered rail item `27` places unpowered `27:0` and
persists.

M185 adds no public API. Detector rail item `28` places unpowered `28:0` and
persists.

M186 adds no public API. Oak stairs item `53` with look yaw `-90` places
`53:0` and persists.

M187 adds no public API. Cobble stairs item `67` with look yaw `-90` places
`67:0` and persists.

M188 adds no public API. Stone slab item `44` places `44:0` and persists.

M189 adds no public API. Bookshelf item `47` places `47:0` and persists.

M190 adds no public API. Jack-o-lantern item `91` with look yaw `-90` places
`91:1` and persists.

M191 adds no public API. Glowstone item `89` places `89:0` in the Overworld
and persists.

M192 adds no public API. Soul sand item `88` places `88:0` in the Overworld
and persists.

M193 adds no public API. Ice item `79` places `79:0` and persists.

M194 adds no public API. Snow block item `80` places `80:0` and persists.

M195 adds no public API. Cobweb item `30` places `30:0` and persists.

M196 adds no public API. Glass item `20` places `20:0` and persists.

M197 adds no public API. White wool item `35` places `35:0` and persists.

M198 adds no public API. Yellow flower `37` on dirt `3` places `37:0` and
persists.

M199 adds no public API. Rose `38` on dirt `3` places `38:0` and persists.

M200 adds no public API. Brown mushroom `39` in a dark dirt pocket places
`39:0` and persists.

M201 adds no public API. Red mushroom `40` in a dark dirt pocket places
`40:0` and persists.

M202 adds no public API. Oak sapling `6` on dirt `3` places `6:0` and
persists.

M203 adds no public API. Snow layer item `78` places `78:0` and persists.

M204 adds no public API. Clay item `82` places `82:0` and persists.

M205 adds no public API. Brick item `45` places `45:0` and persists.

M206 adds no public API. Sponge item `19` places `19:0` and persists.

M207 adds no public API. Sandstone item `24` places `24:0` and persists.

M208 adds no public API. Oak log item `17` places `17:0` and persists.

M209 adds no public API. Oak leaves persist as `18:8` when an adjacent log
`17:0` keeps the decay-check cell alive.

M210 adds no public API. Oak planks item `5` places `5:0` and persists.

M211 adds no public API. Double stone slab item `43` places `43:0` and
persists.

M212 adds no public API. Gold block item `41` places `41:0` and persists.

M213 adds no public API. Iron block item `42` places `42:0` and persists.

M214 adds no public API. Diamond block item `57` places `57:0` and persists.

M215 adds no public API. Lapis block item `22` places `22:0` and persists.

M216 adds no public API. Obsidian item `49` places `49:0` and persists.

M217 adds no public API. Mossy cobble item `48` places `48:0` and persists.

M218 adds no public API. Supported gravel item `13` places `13:0` and persists.

M219 adds no public API. Unprimed TNT item `46` places `46:0` and persists.

M220 adds no public API. Workbench item `58` places `58:0` and persists.

M221 adds no public API. Idle furnace item `61` places `61:2` and persists.

M222 adds no public API. Cobblestone item `4` places `4:0` and persists.

M223 adds no public API. Dirt item `3` places `3:0` and persists.

M224 adds no public API. Overworld netherrack item `87` places `87:0` and persists.

M225 adds no public API. Coal ore item `16` places `16:0` and persists.

M226 adds no public API. Iron ore item `15` places `15:0` and persists.

M227 adds no public API. Gold ore item `14` places `14:0` and persists.

M228 adds no public API. Diamond ore item `56` places `56:0` and persists.

M229 adds no public API. Unlit redstone ore item `73` places `73:0` and persists.

M230 adds no public API. Lapis ore item `21` places `21:0` and persists.

M231 adds no public API. Dispenser item `23` places `23:3` from look yaw `180` and persists.

M232 adds no public API. Chest item `54` places `54:0` and persists.

M233 adds no public API. Note block item `25` places `25:0` and persists.

M234 adds no public API. Sandstone slab item `44` damage `1` places `44:1` and persists.

M235 adds no public API. Wood slab item `44` damage `2` places `44:2` and persists.

M236 adds no public API. Cobble slab item `44` damage `3` places `44:3` and persists.

M237 adds no public API. Stone item `1` places `1:0` and persists.

M238 adds no public API. Grass item `2` places `2:0` and persists.

M239 adds no public API. Supported sand item `12` places `12:0` and persists.

M240 adds no public API. Bed item `355` places foot `26:0` and head `26:8` from look yaw `0` and persists.

M241 adds no public API. Iron door item `330` places lower `71:0` and upper `71:8` and persists.

M242 adds no public API. Lever item `69` places `69:1` on an east face and persists.

M243 adds no public API. Redstone dust item `331` places unpowered wire `55:0` and persists.

M244 adds no public API. Cake item `354` places uneaten cake `92:0` and persists.

M245 adds no public API. Sign item `323` places wall sign `68:5` with Packet130 text and persists.

M246 adds no public API. Log item `17` damage `1` places spruce log `17:1` and persists.

M247 adds no public API. Log item `17` damage `2` places birch log `17:2` and persists.

M248 adds no public API. Wool item `35` damage `1` places orange wool `35:1` and persists.

M249 adds no public API. Wool item `35` damage `4` places yellow wool `35:4` and persists.

M250 adds no public API. Wool item `35` damage `14` places red wool `35:14` and persists.

M251 adds no public API. Wool item `35` damage `15` places black wool `35:15` and persists.

M252 adds no public API. Wool item `35` damage `11` places blue wool `35:11` and persists.

M253 adds no public API. Wool item `35` damage `13` places green wool `35:13` and persists.

M254 adds no public API. Water bucket `326` places still water `9:0` and becomes empty bucket `325`.

M255 adds no public API. Lava bucket `327` places still lava `11:0` and becomes empty bucket `325`.

M256 adds no public API. Chest-minecart item `342` on rail `66` emits Packet23 type `11`.

M257 adds no public API. Furnace-minecart item `343` on rail `66` emits Packet23 type `12`.

M258 adds no public API. Bread item `297` air-use heals Packet8 `15 -> 20` and consumes the stack.

M259 adds no public API. Cooked pork item `320` air-use heals Packet8 `12 -> 20` and consumes the stack.

M260 adds no public API. Apple item `260` air-use heals Packet8 `16 -> 20` and consumes the stack.

M261 adds no public API. Golden apple item `322` air-use heals Packet8 `10 -> 20` and consumes the stack.

M262 adds no public API. Cookie item `357` air-use heals Packet8 `19 -> 20` and consumes the stack.

M263 adds no public API. Mushroom stew item `282` air-use heals Packet8 `12 -> 20` and leaves bowl `281`.

M264 adds no public API. Raw porkchop item `319` air-use heals Packet8 `17 -> 20` and consumes the stack.

M265 adds no public API. Raw fish item `349` air-use heals Packet8 `18 -> 20` and consumes the stack.

M266 adds no public API. Cooked fish item `350` air-use heals Packet8 `15 -> 20` and consumes the stack.

M267 adds no public API. Milk bucket item `335` air-use becomes empty bucket `325` with no health change.

M268 adds no public API. Flint-and-steel item `259` places fire `51:0` on raised
stone; later official random ticks may retain it or extinguish it to air.

M269 adds no public API. Shears item `359` on oak leaves `18:8` emit Packet21 leaf drop; bare hand does not.

M270 adds no public API. Iron helmet item `306` equips into armor slot 5 / Packet5 slot 4.

M271 adds no public API. Gold chestplate item `315` equips into armor slot 6 / Packet5 slot 3.

M272 adds no public API. Diamond leggings item `312` equip into armor slot 7 / Packet5 slot 2.

M273 adds no public API. Chain boots item `305` equip into armor slot 8 / Packet5 slot 1.

M274 adds no public API. Removing stone under gravel `13:0` settles one cell down after official gravity.

M275 adds no public API. Contact with cactus `81:0` drops Packet8 health `20 -> 19`.

M276 adds no public API. Standing in fire `51:0` on netherrack drops Packet8 health `20 -> 19`.

M277 adds no public API. Empty-hand Packet15 opens wooden door halves `64:0/64:8` to `64:4/64:12`.

M278 adds no public API. Empty-hand Packet15 toggles trapdoor `96:3` to open `96:7`.

M279 adds no public API. Empty-hand Packet15 pulses stone button `77:1` to `77:9` and back.

M280 adds no public API. Wool item `35` damage `2` places magenta wool `35:2` and persists.

M281 adds no public API. Wool item `35` damage `3` places light-blue wool `35:3` and persists.

M282 adds no public API. Wool item `35` damage `5` places lime wool `35:5` and persists.

M283 adds no public API. Wool item `35` damage `6` places pink wool `35:6` and persists.

M284 adds no public API. Wool item `35` damage `7` places gray wool `35:7` and persists.

M285 adds no public API. Wool item `35` damage `8` places light-gray wool `35:8` and persists.

M286 adds no public API. Wool item `35` damage `9` places cyan wool `35:9` and persists.

M287 adds no public API. Wool item `35` damage `10` places purple wool `35:10` and persists.

M288 adds no public API. Wool item `35` damage `12` places brown wool `35:12` and persists.

M289 adds no public API. Sapling item `6` damage `1` plants spruce sapling `6:1` on dirt and persists.

M290 adds no public API. Sapling item `6` damage `2` plants birch sapling `6:2` on dirt and persists.

M291 adds no public API. Leaves item `18` damage `1` beside spruce log `17:1` persist as spruce leaves `18:9`.

M292 adds no public API. Leaves item `18` damage `2` beside oak log persist as birch leaves `18:10`.

M293 adds no public API. Sticky piston item `29` places `29:1` and persists.

M294 adds no public API. Piston item `33` places `33:1` and persists.

M295 adds no public API. Wooden plate `72` and stone plate `70` power and unpower together.

M296 widens furnace load/smelt evidence to iron `15→265`, gold `14→266`, and pork `319→320` as well as sand glass.

M297 adds no public API. Personal 2x2 crafts planks `5`, sticks `280`, and torches `50` together.

M298 adds no public API. Workbench crafts the wooden tool family `268+270+271+269+290`.

M299 adds no public API. Workbench crafts the stone tool family `272+273+274+275+291`.

M300 adds no public API. Picks harvest cobble `4`, coal `263`, and diamond `264` together.

M301 adds no public API. Stone axe `275` harvests oak, spruce, and birch logs together.

M302 adds no public API. Gold shovel `284` harvests dirt, sand, gravel, and clay together.

M303 adds no public API. Wheat, sugar cane, and cactus harvests drop together.

M304 adds no public API. Hoe-till `3→60` and trample `60→3` run as one farmland family.

M305 adds no public API. Bonemeal wheat plus cactus and sugar-cane height growth run as one plant family.

M306 adds no public API. Wooden door `64` and trapdoor `96` open-then-close together.

M307 adds no public API. Drowning, suffocation, and lava hurts share one Packet38 family.

M308 adds no public API. Ice break, glass break, and ice melt share one fragile family.

M309 adds no public API. Detector rail `28:8` occupancy and powered rail `27:8` share one rail-power family.

M310 adds no public API. Boat type 1 and minecart type 10 mounts share one vehicle-ride family.

M311 adds no public API. Chest-minecart window and furnace-minecart spawn share one storage-cart family. CHEST windows also accept title `Minecart`.

M312 adds no public API. Redstone torch on `76` inverts to off `75` on the same cell.

M313 adds no public API. Note block `25` plays instruments on stone, wood, and sand together.

M314 adds no public API. Iron helmet `306`, chestplate `307`, leggings `308`, and boots `309` share one armor-craft family.

M315 adds no public API. White wool `35:0` plus dyes `351:1`, `351:2`, and `351:4` share one dyed-wool craft family.

M316 adds no public API. Shears item `359` on oak leaves `18` and a living sheep share one harvest family.

M317 adds no public API. Cobweb `30` and soul sand `88` share one slowdown family.

M318 adds no public API. Gold tools `283-286` and diamond tools `276-279` share one workbench-craft family.

M319 adds no public API. Oak stairs `53`, cobble stairs `67`, and stone slab `44` share one stair-slab craft family.

M320 adds no public API. Leather helmet `298`, chestplate `299`, leggings `300`, and boots `301` share one leather-armor craft family.

M321 adds no public API. Gold helmet `314`, chestplate `315`, leggings `316`, and boots `317` share one gold-armor craft family.

M322 adds no public API. Diamond helmet `310`, chestplate `311`, leggings `312`, and boots `313` share one diamond-armor craft family.

M323 adds no public API. Iron sword `267`, pickaxe `257`, shovel `256`, axe `258`, and hoe `292` share one iron-tool craft family.

M324 adds no public API. Sand `12→20`, cobble `4→1`, and fish `349→350` share one remaining-smelt family.

M325 adds no public API. Compass `345`, clock `347`, and empty map `358` share one navigation-craft family.

M326 adds no public API. Boat `333`, minecart `328`, chest minecart `342`, and furnace minecart `343` share one vehicle-craft family.

M327 adds no public API. Stew `282`, bread `297`, cookies `357`, and cake `354` share one food-craft family.

M328 adds no public API. Bone meal `351:15`, rose red `351:1`, dandelion yellow `351:11`, and gray `351:8` share one dye-craft family.

M329 adds no public API. Fence `85`, ladder `65`, and bookshelf `47` share one utility-block craft family.

M330 adds no public API. Bed occupy `26:8→26:12` and wake standing share one sleep family.

M331 adds no public API. Snowball type 61, egg type 62, and fishing hook type 90 share one throwable family.

M332 adds no public API. Bow `261`, arrows `262`, and Packet23 type 60 share one bow-arrow family.

M333 adds no public API. Dispenser `23:4` plus ejected cobble `4` and planks `5` share one dispenser family.

M334 adds no public API. Gold disc `2256` and green disc `2257` share one record family.

M335 adds no public API. Cake `92:0` through `92:3` share one cake-slice family.

M336 adds no public API. Sandstone `44:1`, wood `44:2`, and cobble `44:3` slabs share one slab-craft family.

M337 adds no public API. Shears `359`, flint-and-steel `259`, and empty bucket `325` share one utility-item craft family.

M338 adds no public API. Coal `263`, oak planks `5`, and lava bucket `327` share one furnace-fuel family.

M339 adds no public API. Oak `6:0`, spruce `6:1`, and birch `6:2` share one sapling-growth family.

M340 adds no public API. Lever `69` latch and stone button `77` pulse share one redstone-input family.

M341 adds no public API. Repeater delay bits `93:3`, `93:7`, `93:11`, and `93:15` share one diode family.

M342 adds no public API. Falling sand type 70 and falling gravel type 71 share one gravity family.

M343 adds no public API. Netherrack fire `51` persist and wool `35` consume share one fire family.

M344 adds no public API. Water bucket `326`/`9` and lava bucket `327`/`11` share one bucket-fluid family.

M345 adds no public API. Gold `41`, iron `42`, diamond `57`, and lapis `22` share one ore-block craft family.

M346 adds no public API. Uncrafting `41`, `42`, `57`, and `22` shares one ore-block uncraft family.

M347 adds no public API. Gold hoe `294` and diamond hoe `293` share one hoe-craft family.

M348 adds no public API. Orange `351:14`, purple `351:5`, and lime `351:10` share one dye-mix family.

M349 adds no public API. Two adjacent chests `54` plus the `Large chest` window share one double-chest family.

M350 adds no public API. Standing sign `63` and wall sign `68` share one sign-text family.

M351 adds no public API. West-face and east-face Packet25 paintings share one orientation family.

M352 adds no public API. Wooden `270`, iron `257`, and gold `285` pick remaining damage share one durability family.

M353 adds no public API. Wood `268`, iron `267`, and diamond `276` sword hits share one sword-damage family.

M354 adds no public API. Dry farmland `60:0` and hydrated `60:7` share one moisture family.

M355 adds no public API. Glass instrument 3 and gold instrument 0 share one remaining note-block family.

M356 adds no public API. Pumpkin `86` and jack-o-lantern `91` share one lantern-craft family.

M357 adds no public API. Glowstone dust `348` to glowstone `89` is the 2x2 dust-craft family.

M358 adds no public API. Snow block `80` craft and shovel harvest of `78`/`80` share one snow family.

M359 adds no public API. Overworld sleep versus Nether bed explode share one bed-dimension family.

M360 adds no public API. Fishing-rod Packet23 type 90 and Packet21 fish 349 share one fishing-catch family.

M361 adds no public API. Two-cell ladder `65:5` place and Packet13 climb share one ladder-climb family.

M362 adds no public API. Adjacent fence `85:0` cells and Packet13 walk-block share one fence-collision family.

M363 adds no public API. Spawner EntityId Zombie and Skeleton Packet24 types 54 and 51 share one hostile-identity family. `B173DedicatedServer.monsters` and `B173SpawnerSeed.entity` stay adapter-local.

M364 adds no public API. Glass `20`, leaves `18:8`, and ice `79` share one translucent sky-light family.

M365 adds no public API. Held compass `345` yaw reversal and two-cell spawn bearing share one compass-point family.

M366 adds no public API. Empty map `358` Packet15 air-use and persist share one map-fill family. The official dedicated server does not rewrite the stack.

M367 adds no public API. Piston `33` extend/retract and sticky `29` pull share one piston-motion family.

M368 adds no public API. Yellow `35:4`, orange `35:1`, and pink `35:6` wool crafts share one remaining dye-wool family.

M369 adds no public API. Cake `92` six-slice eat through air shares one full-eat family, distinct from M335's first three slices.

M370 adds no public API. Cactus, log, and clay smelts share one remaining-furnace family. Smoke-local furnace clicks stay out of the public load identity.

M371 adds no public API. TNT `46`, piston `33`, and sticky piston `29` share one machine-block craft family.

M372 adds no public API. Painting `321`, sign `323`, and bowl `281` share one placeable-item craft family.

M373 adds no public API. Cow milk fill `325→335` and drink `335→325` share one milk-bucket family. `B173SpawnerSeed.cow` and `B173BucketAccess` stay adapter-local.

M374 adds no public API. Apple `260`, cooked pork `320`, and golden apple `322` share one remaining-food-heal family.

M375 adds no public API. Mossy cobble `48`, gold ore `14`, and obsidian `49` share one remaining-pick-break family.

M376 adds no public API. Clay `82`, snow `78`/`80`, and soul sand `88` share one remaining-shovel family.

M377 adds no public API. Powered rail `27:8` and detector rail `28:8` with minecart type 10 share one rail-motion family.

M378 adds no public API. Boat Packet23 type 1 plus Packet39 attach/detach share one boat-water family. Detach uses vehicleId `-1` on the existing Packet39 tracker.

M379 adds no public API. Iron door `71` halves and lever `69` power share one iron-door family.

M380 adds no public API. Trapdoor `96` four wall faces share one trapdoor-toggle family.

M381 adds no public API. TNT `46` flint-steel prime, Packet23 type 50, and Packet60 strength 4 share one TNT-prime family.

M382 adds no public API. Fourteen obsidian `49` cells and six portal `90` cells share one portal-frame family. Traversal is not claimed.

M383 adds no public API. Brown mushroom `39` on dirt and red mushroom `40` on netherrack share one mushroom-place family.

M384 adds no public API. Cactus `81` on sand and sugar cane `83` beside water share one desert-plant growth family.

M385 adds no public API. Oak, spruce, and birch log-backed leaves share one leaf-decay family.

M386 adds no public API. Ice `79` to water `9` and snow `78` to air share one light-melt family.

M387 adds no public API. Glowstone `89`, jack-o-lantern `91:1`, and floor torch `50:5` share one remaining-light family.

M388 adds no public API. Zombie feather `288` and skeleton arrow `262` share one hostile-drops family.

M389 adds no public API. Cow leather `334` and chicken feather `288` share one animal-drops family.

M390 adds no public API. Creeper type `50` and spider type `52` share one remaining-spawner identity family.

M391 adds no public API. Creeper Packet60 strength `3` destroying dirt `3` and wool `35` shares one creeper-explode family.

M392 adds no public API. Still water `9` and still lava `11` horizontal flow share one remaining-fluid-flow family.

M393 adds no public API. Oak stairs `53` and cobble stairs `67` look-yaw facings share one stair-facing family.

M394 adds no public API. Sandstone, wood, cobble, and double slabs share one remaining-slab-place family.

M395 adds no public API. Cyan, pink, and light-blue personal 2x2 mixes share one remaining-dye-mix family.

M396 adds no public API. Magenta, light-blue, and lime dyed wool share one remaining-wool-crafts family.

M397 adds no public API. Dispenser snowball type `61` and egg type `62` share one dispenser-projectile family.

M398 adds no public API. Gold disc `2256` and cat disc `2257` jukebox ejects share one jukebox-eject family.

M399 adds no public API. Stone button `77` east, west, south, and north wall faces share one remaining-button family. Beta 1.7.3 has no wooden button.

M400 adds no public API. Wall torch `50:1` through `50:4` share one remaining-torch-faces family.

M401 adds no public API. Redstone wire `55` cross, line, and elbow shapes share one remaining-wire family.

M402 adds no public API. Detector rail `28:0` occupied to `28:8` by minecart type `10` shares one remaining-detector family.

M403 adds no public API. Two Packet23 type `1` boats broken to plank `5` and stick `280` share one remaining-boat-break family.

M404 adds no public API. Minecart type `10` and chest-cart type `11` Packet7 breaks share one remaining-cart-break family.

M405 adds no public API. Pig type `90` saddle `329` consume plus Packet39 mount share one pig-saddle family.

M406 adds no public API. Dyed sheep wool `35:14` and `35:4` share one sheep-dye family.

M407 adds no public API. Chicken type `93` plus egg `344` / Packet23 type `62` share one chicken-egg family.

M408 adds no public API. Squid type `94` in water plus ink sac `351:0` share one squid-ink family.

M409 adds no public API. Spider type `52` string `287` plus cobweb `30` share one spider-web family.

M410 adds no public API. Nether ghast type `56` plus fireball type `63` share one ghast-fireball family.

M411 adds no public API. Nether pigman type `57` plus cooked pork `320` share one zombie-pigman family.

M412 adds no public API. Slime type `55` parent death plus child type `55` share one slime-split family.

M414 adds no public API. Still lava `11` plus water `9` hardening two obsidian `49` share one lava-obsidian family.

M415 adds no public API. Flowing lava `10` plus water `9` hardening two cobble `4` share one water-cobble family.

M416 adds no public API. Bookshelf `47` craft, two-cell place, and empty harvest share one remaining-bookshelf family.

M417 adds no public API. Two TNT `46` cells plus flint-and-steel Packet60 chain share one remaining-TNT family.

M418 adds no public API. Four-cell obsidian `49` frame fragment plus pick harvest share one remaining-obsidian family.

M419 adds no public API. Nether netherrack `87`, soul sand `88`, and glowstone `89` share one remaining-netherrack family.

M413 adds no public API. Netherrack fire `51` catching planks `5`, leaves `18`, and wool `35` share one fire-spread family.

M433 adds no public API. Isolated look-yaw chest `54` plus EW and NS pairs share one remaining-chest-orient family.

M434 adds no public API. Sponge `19`, glass `20`, and ice `79` share one remaining-sponge-glass-ice family.

M441 adds no public API. Cookie `357` plus mushroom stew `282` share one remaining-food-rest family.

M420 adds no public API. Wolf type `95` bone tame plus dye collar share one wolf-tame family.

M421 adds no public API. Creeper gunpowder `289` plus TNT `46` craft share one creeper-gunpowder family.

M423 adds no public API. Size-1 slimeball `341` plus sticky piston `29` craft share one slimeball family.

M424 adds no public API. Furnace-cart type `12` coal push plus detector `28:8` share one furnace-cart-motion family.

M425 adds no public API. Dispenser `23`, furnace `61`, and pumpkin `86` remaining facings share one remaining-machine-faces family.

M426 adds no public API. Lever `69` remaining wall/ground faces plus repeater `93/94` delay-facing share one remaining-redstone-faces family.

M427 adds no public API. Piston `33` and sticky piston `29` remaining facings share one remaining-piston-orient family.

M428 adds no public API. Wooden door `64` remaining hinge/face pairs share one remaining-door-orient family.

M429 adds no public API. Ladder `65`, trapdoor `96`, and wall sign `68` remaining west/south/north faces share one remaining-attach-faces family.

M430 adds no public API. Remaining Packet25 motive-size walls 4x2, 4x3, and 4x4 share one remaining-painting-motives family.

M431 adds no public API. Remaining bed `26` west/north/east halves share one remaining-bed-orient family.

M432 adds no public API. Remaining rail `66` slope `2` plus curve `6` share one remaining-rail-geometry family.

M435 adds no public API. Remaining natural Packet24 types among `50/51/52/54` share one remaining-natural-spawns family.

M437 adds no public API. Overworld pig type `90` plus Nether pigman type `57` share one lightning-pig identity family.

M438 adds no public API. Clock `347` plus empty map `358` air-use share one remaining-clock-map family.

M439 adds no public API. Coal ore `16`, lapis ore `21`, and unlit redstone ore `73` share one remaining-ore-place family.

M440 adds no public API. Remaining 2x2 dye mixes `351:7` and `351:13` share one remaining-dye-rest family.

M442 adds no public API. Two jukebox `84` cells plus gold/green discs share one remaining-record-place family.

M443 adds no public API. Empty-bucket pickup of still versus flowing water and lava share one remaining-bucket-rest family.

M444 adds no public API. Pig pork `319` plus sheep undyed wool `35:0` death drops share one remaining-mob-drops-rest family.

M422 adds no public API. Skeleton bone `352` plus bone-meal wheat `59:7` share one skeleton-bone family.

M436 adds no public API. Bow `261` Packet23 type `60` land plus arrow `262` collect share one remaining-arrow-life family.

M445 adds no public API. Skeleton type `51` Packet23 type `60` arrows with skeleton thrower share one skeleton-ranged-ai family.

M450 adds no public API. Nether pigman type `57` Packet7 group-aggro share one pigman-anger family.

M453 adds no public API. Void Packet8 `0` plus seeded hotbar Packet21 drops share one player-death-drops family.

M454 adds no public API. Peaceful absence versus Easy persist of Packet24 `50/51/54` share one peaceful-despawn family.

M455 adds no public API. Zombie type `54` and skeleton type `51` Packet31/33/34 pursuit toward pose share one melee-pursuit family.

M448 adds no public API. Creeper type `50` Packet40 ignited state before Packet60 share one creeper-fuse family.

M452 adds no public API. Zombie type `54` Packet8 knockback plus hurt-time cooldown share one knockback-cooldown family.

M457 adds no public API. Spider type `52` leap toward pose plus Packet8 share one spider-leap family.

M458 adds no public API. Slime type `55` size-1 plus larger Packet8 contact share one slime-touch family.

M459 adds no public API. Nether ghast type `56` Packet23 type `63` Packet60 strength `1` hit share one ghast-fireball-hit family.

M460 adds no public API. Bed occupy `26:12` plus type `54` interrupt leave share one monster-bed-interrupt family.

M462 adds no public API. Player bow `261` Packet23 type `60` Packet38 hits on pig `90` and zombie `54` share one bow-mob-hit family.

M467 adds no public API. Easy then Hard type `54` Packet8 `20->18` share one difficulty-damage family.

M469 adds no public API. Void walk-off Packet8 `0` plus Packet9 respawn health `20` share one void-death family.

M554 adds no public API. Extended piston `33:12` Packet14 base break plus head-`34` leftover air share one extended-head-break family.

M546 adds no public API. Power-above piston `33:4 -> 33:12` plus retract with `direct-power=false` share one piston-QC family.

M547 adds no public API. Power-above sticky `29:4 -> 29:12` plus payload pull share one sticky-piston-QC family.

M548 adds no public API. Neighbor-update piston pulse `33:4 -> 36:4 -> 33:4` with `power=none` shares one piston-BUD family.

M550 adds no public API. Power-above dispenser `23` Packet21 cobble eject with no adjacent lever shares one dispenser-QC family.

M552 is retracted. Its adjacent powered solid did not isolate TNT quasi-connectivity, so no TNT-QC family is published by the current release.

M553 adds no public API. Piston `33` rejection of chest `54`, furnace `61`, and spawner `52` shares one piston-immovable family.

M549 is retracted. A hidden fallback could place an ordinary south power source while preserving the same trace, so no sticky-BUD family is published by the current release.

M559 adds no public API. Sequenced sticky `29` then piston `33` two-cell cobble travel shares one double-extender family.

M560 adds no public API. Packet9 `0 -> -1` pose within 128 of Overworld `xz/8` shares one portal-scale family.

M564 is retracted. Its negative changed cover and substrate as well as light, so no causal spawn-light family is published by the current release.

M557 is retracted. The fixture did not observe the repeater or piston at tick resolution, so no one-tick-pulse family is published by the current release.

M566 adds no public API. Packet53 dirt `3 -> 2` on lit samples plus covered dirt stay shares one grass-spread family.

M555 adds no public API. Its corrected scope is the directly observed rapid-activation wall-torch sequence `76:4 -> 75:4 -> 76:4`; it does not claim an observed eight-toggle threshold or 100-tick window.

M567 adds no public API. Bed occupy plus cactus Packet8 `0` Packet9 at the bed shares one bed-spawn family.

M556 adds no public API. Complementary wall-torch states that remain after SET and RESET are disabled share one RS-NOR latch family.

M563 adds no public API. A shifted Nether return that creates a non-source 14-obsidian/6-portal Overworld frame shares one Nether-exit-create family.

M569 adds no public API. Out-of-range absence followed by in-range Packet24 type `54` after a `Delay=1` rewrite shares one spawner-delay family.

M562 and M568 add no family. They failed central serialized runtime qualification and remain rejected evidence.
