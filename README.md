# Worldline

Current official milestone: **Worldline v1.353.0 - M365 Compass point set (GO)**.

Worldline is an experimental controlled runtime for Minecraft Beta 1.7.3. Its
first goal is deliberately small: boot the real game headlessly, load a world,
advance one externally controlled tick, and compare the observable result with
the official vanilla artifact.

Worldline includes a neutral optimization metadata SDK: a source-only
`OptimizationRef`, a portable record schema, and a fail-closed validator.
Project-specific catalogs stay in the repository that owns the implementation;
Worldline experiments reference external IDs without copying their records.
See `docs/OPTIMIZATION_SDK.md`.

The controlled-runtime baseline and first laboratory cycle are complete. The repository contains a
small public lifecycle contract, an independently compiled kernel, an
independent canonical-trace protocol, and a fail-closed verification gate. Its
strongest smoke constructs the real mapped client tick object graph without a
window, loads a deterministic in-memory world, advances exactly one requested
`Minecraft.runTick()`, and matches the equivalent execution from the official
client JAR through 16 ticks in four fresh JVMs. A second two-process lab proof
adds replay-backed checkpoints, restoration, branching, semantic inventory GUI
control, and descriptor-selected independently packaged benchmark mod JARs.

The stable M3 surface adds `AutomatedMinecraftRuntime`, `GameWorld`,
`GamePlayer`, `GameEntity`, `BlockPosition`, `BlockState`, and `GamePosition`.
It supports neutral world time and block access, block mutation, active-entity
enumeration, player identity/state, teleportation, and hotbar selection. See
`docs/M3_API.md` for lifecycle rules and non-claims.

M4 adds `SnapshotMinecraftRuntime` and immutable `RuntimeSnapshot` artifacts.
The b1.7.3 adapter captures a canonical replay-backed document with an embedded
checksum, restores it in a fresh runtime, and rejects corrupt, non-canonical,
wrong-version, or wrong-runtime input. See `docs/M4_SNAPSHOT.md` for the exact
format and portability boundary.

M5 adds a canonical `ReproductionBundle` that embeds the M4 snapshot and
declares the exact Worldline version, runtime ID, official client SHA-256, and
RetroMCP revision required to replay it. A neutral CLI loads a runtime provider
and restores the bundle without distributing the official JAR or mapped game
classes. See `docs/M5_BUNDLE.md` for the format and command contract.

M6 adds a strict parser for schema-bearing `v2` state traces, a stable tabular
viewer, and a first-divergence analyzer that reports the earliest seed, schema,
record, or field mismatch with exact indices and values. Both viewer and diff
are available through the neutral CLI without requiring Minecraft runtime
inputs. See `docs/M6_TRACE.md` for ordering and exit-code semantics.

M7 adds strict, bounded mod-JAR descriptors, SHA-256 provenance, explicit
runtime/API compatibility, and isolated descriptor-selected entrypoint loading.
The neutral CLI can inspect compatibility without executing mod code or loading
Minecraft. See `docs/M7_MODS.md` for the exact package and trust boundary.

M8 adds canonical `.wlmtest` results that bind an exact mod artifact and
descriptor to a canonical state trace. The neutral CLI records results and
compares mod versions with M6 first-divergence semantics. See
`docs/M8_RESULTS.md` for the result format and attestation boundary.

M9 adds canonical `.wlscenario` artifacts, exact first-divergence fingerprints,
and a deterministic budgeted delta debugger that proves one-minimality through
final individual-removal checks. See `docs/M9_MINIMIZATION.md` for guarantees
and the adapter/evaluator boundary.

M10 adds a real offscreen graphics boundary: Minecraft's `Tessellator` draws
through hash-pinned LWJGL and a native Windows OpenGL Pbuffer, then the gate
reads and hashes the RGBA framebuffer. Two mapped and two official-JAR processes
must agree. The original Aero artifact was not available, so its runtime
compatibility is explicitly `NOT_RUN`; see `docs/M10_RENDER.md`.

M11 qualifies the supplied Aero Model Lib 3.0.0 source revision: all 222 core
tests pass, the StationAPI library and test consumer build, Fabric Loader boots
the real client with both Aero test entrypoints, and an isolated probe loads
the built JAR. A neutral analyzer now distinguishes a slow frame caused by
expanded named work counters from a runtime stall with stable work. See
`docs/M11_ATTRIBUTION.md` for the bounded contract and non-claims.

M12 drives two real dense Aero clients from a fixed seed, chunk set, camera,
and 240-tick window. Both captures reproduce a frame spike with at least 10 ms
inside chunk compilation, and each record window minimizes to one
qualifying frame. The generated save is captured, but the current test
fixture persistence was not yet distinguished; see
`docs/M12_CAPTURE.md` for that boundary and the historical-spike non-claim.

M13 resolves that boundary: 576 real entity blocks persist while roughly half
of the fresh world's global BlockEntity list is phantom state. Substantial
chunk-compilation pressure remains with the Aero fixture disabled, and
exploratory runs spike in both modes; dense amplification is not established. Aero's
compile governor is rejected in this path because an always-active control
causes tens of millions of immediate retries. See `docs/M13_DIFFERENTIAL.md`.

M14 corrects the caller model and isolates the stable-camera pressure. The
principal call is non-forced, `false` triggers another invocation before the
frame deadline, and a fresh empty world still has thousands of dirty chunk
builders after warmup. A smoke-only policy performs two real priority-ordered
rebuilds once per frame and returns `true`, eliminating immediate retries while
explicitly trading for slower queue drainage. It remains experimental; see
`docs/M14_CHUNK_BACKLOG.md`.

M15 replaces the Boolean ambiguity experimentally with explicit complete,
accepted/deferred, and stalled/deferred outcomes, then measures the cost from
the first world frame. The contract eliminates same-frame retries and produces
broad exact chunk-geometry agreement, but a fixed two-rebuild batch leaves far
more visible chunks dirty than vanilla. That policy is rejected; see
`docs/M15_CHUNK_CONTRACT.md`.

M16 keeps that explicit boundary and selects visible dirty work with an adaptive
2/4/6/8 accepted-work envelope plus a 12 ms rebuild budget. It preserves one
caller completion per frame, closes the initial visible-readiness gap, and
substantially reduces the observed worst frame. A canonical save snapshot and
frozen tick require baseline and candidate to drain all chunk work and satisfy
a strict whole-frame pixel comparison. The policy is qualified as an adapter
candidate, not an Aero release; see `docs/M16_ADAPTIVE_CHUNKS.md`.

M17 runs that candidate against stationary-empty, stationary-dense, and
moving-dense saves alongside vanilla and Aero's rejected governor. Adaptive
preserves one completion per frame and drains background work, but moving
readiness/timing remain observational; non-preemptive budget overshoot and three strict
framebuffer divergences block promotion. Its evaluation profile is packaged
default-off and marked lab-only NO-GO; see `docs/M17_SCHEDULER_HARDENING.md`.

M18 turns the capture skip-saves flag back on for one restored dense twin and
injects a non-forced world save at a known tick. The skipped twin cancels it;
the live twin places that save on the same compile/GC/heap timeline. The
historical random spike remains a non-claim; see `docs/M18_SAVE_ATTRIBUTION.md`.

M19 forces a 60-chunk dirty set beside the tower and compares vanilla's
non-forced save batch with a default-off one-chunk cap and a save-cancelled
control. The cap reduces the worst synthetic save without claiming the real
historical spike is eliminated; see `docs/M19_FORCED_AUTOSAVE.md`.

M20 adds the unmodified official Beta 1.7.3 dedicated server as a second
hash-pinned local artifact. Two fresh localhost-only server processes reach
the native `Done` marker, accept `stop`, save, and exit cleanly. This is the
foundation for server instrumentation and multiplayer tests; it does not yet
connect a client or claim multiplayer determinism. See
`docs/M20_SERVER_BOOTSTRAP.md`.

M21 adds neutral `DedicatedServerRuntime` and immutable `ServerState` contracts.
The b1.7.3 adapter boots the official process, sets world time through the
native console, forces a save, reads the persisted gzip/NBT `Time` tag, and
requires clean shutdown. Two fresh controller/server pairs qualify that path;
see `docs/M21_SERVER_CONTROL.md`.

M22 adds neutral multiplayer session/state contracts and an original headless
protocol-14 client. Two fresh scenarios each connect to an official localhost
server, complete the native offline handshake/login, appear in the server's
player list, disconnect, and disappear cleanly. See
`docs/M22_MULTIPLAYER_WIRE.md`.

M23 observes the official server state produced by those sessions. After
login/disconnect and a forced save, the b1.7.3 adapter reads the generated
player NBT through a neutral immutable value and verifies dimension, finite
position, health, and inventory. See `docs/M23_PLAYER_PERSISTENCE.md`.

M24 advances the wire session into the play channel. It consumes the bounded
official spawn/time/chunk prelude, acknowledges the server's initial position,
sends a deliberate look packet, and requires the exact yaw/pitch in persisted
player NBT across two fresh servers. See `docs/M24_PLAY_POSE.md`.

M25 sends a collision-bounded relative movement through that play channel. A
`+0.125 X` displacement remains inside the spawn block footprint, and two fresh
official servers must persist the exact requested target. See
`docs/M25_PLAYER_MOVEMENT.md`.

M26 composes a synchronized protocol-14 session with the native renderer. Two
fresh clients connect to official servers and render connected state through
Minecraft `Tessellator`, LWJGL, and an offscreen Pbuffer, matching the
M10-qualified frame exactly. See `docs/M26_NATIVE_MULTIPLAYER.md`.

M27 runs two clients simultaneously and adds a bounded inbound packet pump.
`WorldlineA` sends native chat through an official server and `WorldlineB`
receives the exact broadcast after queued chunk/entity traffic. See
`docs/M27_MULTIPLAYER_CHAT.md`.

M28 turns the first inbound chunk packet into a neutral observation. Two fresh
clients consume official `Packet51` envelopes and verify bounded full
`16 x 128 x 16` regions without exposing compressed bytes or mapped classes.
See `docs/M28_REMOTE_CHUNK.md`.

M29 strictly inflates that payload and exposes immutable coordinate-based block
state and light queries. A mapped vanilla `NibbleArray` oracle checks all
32,768 coordinates of a fixture, then two fresh official servers provide real
full chunks. See `docs/M29_REMOTE_CHUNK_SNAPSHOT.md`.

M30 preserves native prechunk load/unload events in the shared inbound pump and
assembles decoded snapshots into an immutable, 256-region neutral world view.
A lifecycle oracle proves eviction and bounds; two official servers each
provide a lifecycle-qualified addressable chunk. See `docs/M30_REMOTE_WORLD_CACHE.md`.

M31 applies Packet52/53 block changes by immutable cache replacement. Two
operator-qualified clients break nearby blocks, but Worldline accepts success
only when each official server reports the exact target as air. See
`docs/M31_INCREMENTAL_WORLD.md`.

M32 sustains the vanilla protocol-14 heartbeat for 40 ticks, grows the decoded
remote cache, and renders an exact block slice through mapped Minecraft
`Tessellator` in a native offscreen context. A Packet53 block change must alter
the corresponding native pixel. See `docs/M32_REMOTE_TERRAIN_RENDER.md`.

M33 rises to collision-free clearance and crosses two chunk boundaries in
quarter-block movement steps. The official server must turn over the bounded remote cache, and native before/after chunk
maps must clear one removed pixel and add one loaded pixel. See
`docs/M33_CHUNK_TRAVERSAL.md`.

M34 decodes the official server's Packet13 correction, acknowledges its exact
wire order, and replaces the neutral session pose and stance. Two fresh clients
deliberately enter a decoded solid block, return exactly to their initial
server-authoritative poses, and retain the original cached chunk. See
`docs/M34_POSE_CORRECTION.md`.

M35 exposes bounded movement outcomes as `UNCHALLENGED` or `CORRECTED`. Two
fresh sessions persist a small unchallenged move, then force collision rollback
to that accepted pose without losing the remote cache. See
`docs/M35_MOVEMENT_OUTCOME.md`.

M36 composes those outcomes into an immutable three-step route. After a solid
collision corrects the middle step, the final relative movement continues from
the authoritative pose and persists successfully. See
`docs/M36_ROUTE_RECOVERY.md`.

M37 adds explicit `CONTINUE` and `STOP_ON_CORRECTION` route policies. The stop
policy ends immediately after a corrected obstacle with no retry and leaves a
later step absent from persisted server state. See `docs/M37_ROUTE_POLICY.md`.

M38 adds caller-supplied primary/fallback pairs. A fallback is skipped after an
unchallenged primary and executes once after a corrected primary, with no
automatic retry or path discovery. See `docs/M38_EXPLICIT_FALLBACK.md`.

M39 emits synchronous indexed observations immediately after each resolved
primary or fallback attempt. Events remain on the caller thread and retain the
same outcome objects returned by the route. See `docs/M39_ROUTE_OBSERVATION.md`.

M40 lets a caller-thread controller return `CONTINUE` or `STOP` immediately
after an immutable route event. A stop prevents every later caller-supplied
attempt without adding retries or asynchronous control. See
`docs/M40_OBSERVER_CONTROL.md`.

M41 returns an immutable execution summary that identifies the exact terminal
event and distinguishes `EXHAUSTED` from `CONTROLLER_STOP`. See
`docs/M41_ROUTE_TERMINATION.md`.

M42 carries one opaque caller-owned reference by identity through synchronous
route events and the terminal execution summary, without a global registry.
See `docs/M42_ROUTE_CORRELATION.md`.

M43 executes at most 16 correlated route plans sequentially and returns an
immutable per-route batch result with an explicit batch termination. See
`docs/M43_CORRELATED_BATCH.md`.

M44 emits caller-thread batch events with a stable route index while retaining
each correlated route event and its internal indexes. See
`docs/M44_BATCH_OBSERVATION.md`.

M45 lets a batch-wide controller stop at an indexed movement event, before
later alternatives or plans are sent. See `docs/M45_EVENT_BATCH_STOP.md`.

M46 returns the exact terminal kind and last resolved batch event for event
stop, after-route stop, and exhaustion. See `docs/M46_BATCH_TERMINAL.md`.

M47 exposes immutable completed-route, outcome, and correction counts without
flattening the identity-bound result graph. See `docs/M47_BATCH_COUNTS.md`.

M48 exposes immutable server-authoritative inventory windows and applies
matching incremental slot updates. See `docs/M48_INVENTORY_OBSERVATION.md`.

M49 selects a bounded held hotbar slot and proves the authoritative carried
item through an independent named protocol peer. See `docs/M49_HELD_ITEM_PEER.md`.

M50 drops the current held stack through Packet14 status 4 and proves the
resulting empty local slot, empty peer-carried item, and clean persisted
inventory. See `docs/M50_DROP_HELD_ITEM.md`.

M51 decodes the resulting Packet21 item entity into an immutable stack,
fixed-point position, and bounded launch velocity observed by an independent
client. See `docs/M51_DROPPED_ITEM_SPAWN.md`.

M52 follows that exact entity through Packet22 collection by a validated named
player and Packet29 terminal removal, then proves the restored live and saved
inventory. See `docs/M52_ITEM_COLLECTION.md`.

M53 places the selected observed block through Packet15 and proves the exact
Packet53 stone replacement in two immutable remote caches, plus consumed local,
peer, and persisted inventory. See `docs/M53_HELD_BLOCK_PLACEMENT.md`.

M54 activates a placed single chest with an empty selected hand, decodes the
Packet100 modified-UTF descriptor, and pairs it with the exact 63-slot Packet104
combined view. See `docs/M54_CHEST_WINDOW.md`.

M55 stages exact personal-window left-click predictions and commits them only
after correlated Packet106 accepted acknowledgements. An independent peer
proves the server-side held-stack transitions. See
`docs/M55_ACCEPTED_PERSONAL_TRANSACTION.md`.

M56 handles Packet106 false by immediately acknowledging re-enable, staging the
ordered Packet104/cursor Packet103 correction, and atomically publishing the
recovered state. See `docs/M56_REJECTED_TRANSACTION_RECOVERY.md`.

M57 composes accepted clicks into one bounded personal 2x2 recipe: log
`17x1:0` becomes planks `5x4:0`. Four accepted actions stage the grid, result,
consumption, and storage; an M56 recovery audit then proves the official state.
See `docs/M57_PERSONAL_CRAFTING.md`.

M58 closes the active remote chest with Packet101, then requires an accepted
no-op Packet102 on personal window 0 before publishing immutable closure
evidence. See `docs/M58_WINDOW_LIFECYCLE.md`.

M59 transfers an occupied personal slot into an empty single-chest slot through
two accepted container actions, reconciles both inventory views and the cursor,
then proves chest persistence by reopening after a clean server restart. See
`docs/M59_CHEST_TRANSFER.md`.

M60 opens a typed furnace, loads exact sand and coal through four accepted
container actions, then correlates asynchronous Packet103 slot changes with the
full Packet105 cook/burn progression to one glass output. See
`docs/M60_FURNACE_SMELT.md`.

M61 retrieves that glass through accepted actions 5 and 6, requires the exact
Packet200 crafted-stat side effect, and proves player/output persistence after
a clean restart. See `docs/M61_FURNACE_OUTPUT.md`.

M62 opens the official workbench as a typed read-only window and preserves its
asymmetric wire layout: Packet100 declares nine matrix slots while Packet104
contains result, matrix, and player tail for 46 total slots. See
`docs/M62_WORKBENCH_WINDOW.md`.

M63 left-takes three planks and right-places one into each slot of the first
workbench row through four accepted transactions. It models the recipe result
locally and rejects close while the grid remains occupied. See
`docs/M63_WORKBENCH_PREPARATION.md`.

M64 confirms that model through an accepted exact slabs output prediction,
observes the crafted-stat side effect, consumes the grid, stores the result,
closes safely, and proves persistence after restart. See
`docs/M64_WORKBENCH_OUTPUT.md`.

M65 equips the exact leather set through accepted personal-window actions,
observes every reversed armor mapping through a named peer's Packet5 stream,
and proves local and peer state after restart. See `docs/M65_PEER_ARMOR.md`.

M66 composes that armor with one named diamond-sword PvP strike, separates the
attacker's Packet38 proof from the victim's Packet38/Packet8 health transition,
and persists health 18. See `docs/M66_PLAYER_COMBAT.md`.

M67 reopens an M59 chest, retrieves exact stone through two accepted active-
container actions, atomically updates combined and personal views, and proves
empty chest plus player persistence after restart. See
`docs/M67_CHEST_RETRIEVAL.md`.

M68 drives the pinned real StationAPI/Aero client through vanilla multiplayer
login, observes Packet1/13/51 readiness, renders bounded remote-world frames,
and requires post-ready Aero logs before a clean disconnect. See
`docs/M68_AERO_MULTIPLAYER_LOGIN.md`.

M69 emits the exact authenticated Packet18 held-item swing boundary and
correlates the resulting animation-1 observation to a Packet20-named peer. It
does not treat the animation as an attack acknowledgment. See
`docs/M69_PEER_SWING.md`.

M70 composes M66, M68, and M69 in one official-server session: the real Aero
observer applies Packet18 before Packet38, then completes a bounded frame/log
window. It makes no spike or performance-causality claim. See
`docs/M70_AERO_COMBAT_WINDOW.md`.

M71 repeats matched fresh control/event arms around one exact Packet3 anchor,
balances order across four pairs, and reports only descriptive distributions of
logger-selected Aero rows and pair deltas. It makes no causal, significance, or
performance-regression claim. See `docs/M71_PAIRED_AERO_WINDOW.md`.

M72 loads one server-safe StationAPI content definition without Aero on the
server, transfers one server-only block-entity nonce through an explicit content
message, and renders the exact remote block through a real pinned Aero client.
It qualifies one fixture, not generic synchronization or performance. See
`docs/M72_AERO_SERVER_CONTENT.md`.

M73 activates the same registered content mod in two balanced absent/present
pairs. The present arm synchronizes and renders sixteen exact instances while
the absent arm preserves the same plan and trigger with zero mutations. A
tracked-plan acknowledgement replaces timing-only placement readiness. Dynamic
row summaries remain descriptive only. See `docs/M73_PAIRED_AERO_CONTENT.md`.

M74 keeps the same paired structural fixture but replaces selected logger rows
with a preallocated in-memory census of every complete renderer interval in the
bounded bracket. Binary artifacts are flushed and strictly parsed only after
measurement; summaries and pair deltas remain descriptive. See
`docs/M74_COMPLETE_AERO_CENSUS.md`.

M75 keeps all sixteen synchronized entities and renderer dispatches constant,
then forwards nested subsets of `0/1/4/16` calls into Aero under two mirrored
fresh ladders. It qualifies exact exposure acquisition and descriptive census
summaries, not a monotonic, causal, or density-response result. See
`docs/M75_AERO_DENSITY_LADDER.md`.

M76 keeps the sixteen-entity fixture constant and splits it into renderer
registration removed, renderer body without Aero, and renderer body with Aero.
Exact `0/0`, `16/0`, and `16/16` per-record call pairs qualify the structural
decomposition. Mixed mirrored timings do not establish stable stage cost or
causal attribution. See `docs/M76_RENDERER_DECOMPOSITION.md`.

M77 keeps the same synchronized scene and directly times the complete renderer,
its sixteen nested Aero `queueAtRest` direct-fallback calls, and both empty-page
flush calls. A post-seal binary sidecar is index-bound to every M74 census
record. The spans are descriptive under timing instrumentation, not paged,
additive, or causal costs.
See `docs/M77_DIRECT_STAGE_TIMING.md`.

M78 adds a client-only Aero marker to the remote presentation class while the
common/server class remains Aero-free. The same scene now produces sixteen real
enqueues and four cached page calls with no fallback or rebuild in every
record. Direct steady-state spans remain descriptive. See
`docs/M78_PAGED_STAGE_TIMING.md`.

M79 disposes that exact warmed model once after 300 retained records and proves
cache `4 -> 0 -> 4`, four deleted/compiled page deltas, and exactly four
rebuilds without fallback in the aligned event record. Event and warmed spans
remain descriptive. See `docs/M79_COLD_PAGE_REBUILD.md`.

M80 replaces explicit disposal with one server-authored removal. An exact
acknowledged cell becomes air, live renderer membership changes `16 -> 15`,
and the first altered record carries one natural page rebuild with zero direct
fallback. Request latency and timings remain descriptive; generic invalidation
and stale BE cleanup are not claimed. See
`docs/M80_NATURAL_MEMBERSHIP_REBUILD.md`.

M81 moves the fixed scene across a natural page boundary and removes exact
indices zero and eight in one acknowledged request. Both client blocks become
air, membership changes `16 -> 14`, and the first altered record carries two
page rebuilds with two cached pages and zero fallback. See
`docs/M81_NATURAL_MULTIPAGE_REBUILD.md`.

M82 holds that two-page scene constant across three fresh arms and removes
exact target sets of one, two, and four members. Membership becomes 15, 14,
and 12 while event rebuilds are 1, 2, and 2: within this bounded fixture,
rebuild count tracks affected pages rather than removed-member count. See
`docs/M82_NATURAL_WAVE_LADDER.md`.

M83 holds removed-member count at two and contrasts indices `0,1` within one
page against `0,4` across two pages. Both arms end at membership fourteen;
event rebuilds are one versus two, with two cached pages and no fallback. See
`docs/M83_PAGE_TOPOLOGY_CONTRAST.md`.

M84 moves the fixed-cardinality contrast to four natural page keys. It removes
three exact members from one cached page or one member from each of three
cached pages while preserving membership thirteen, three cached pages/page
calls, one singleton direct fallback, and two flush calls. Event rebuilds are
exactly one versus three; timings remain descriptive. See
`docs/M84_FOUR_PAGE_TOPOLOGY_CONTRAST.md`.

M85 removes one exact synchronized cell and restores that same server-authored
identity after thirty retained records. The topology moves from four cached
pages to three cached pages plus one direct fallback, then recovers four cached
pages with exactly one rebuild. See `docs/M85_NATURAL_MEMBERSHIP_RECOVERY.md`.

M86 repeats that same-cell recovery with two explicit generations. Four typed
requests prove `16 -> 15 -> 16 -> 15 -> 16`, the same fallback topology during
both removals, and one rebuild at each restoration. See
`docs/M86_REPEATED_MEMBERSHIP_RECOVERY.md`.

M87 recovers index zero and then adjacent index one. It proves fallback on the
first removal and immediate rebuild on the second, while both restores rebuild
once. See `docs/M87_TWO_CELL_MEMBERSHIP_RECOVERY.md`.

M88 reverses those exact cells and preserves their distinct behavior: index one
rebuilds even when first, while index zero falls back even when second. See
`docs/M88_REVERSE_TWO_CELL_RECOVERY.md`.

M89 recovers index four, the other identity in index zero's pinned natural
two-member page, and reproduces the same fallback/restore-rebuild path. See
`docs/M89_SIBLING_CELL_RECOVERY.md`.

M90 recovers index two, a sibling of index one in a pinned six-member page, and
reproduces the same fully batched rebuild path. See
`docs/M90_LARGER_PAGE_SIBLING_RECOVERY.md`.

M91 removes both qualified identities from that six-member page before
restoring them in reverse, preserving the fully batched rebuild route at all
four transitions. See `docs/M91_LARGER_PAGE_DEPLETION_RECOVERY.md`.

M92 removes the third identity in that exact page too, reducing it from six to
three members before reverse recovery while preserving the batched route. See
`docs/M92_THIRD_MEMBER_DEPLETION_RECOVERY.md`.

M93 removes all six identities from that exact page, then restores them in
reverse. It qualifies the two-member batched threshold, one-member direct
fallback, empty-page boundary, and symmetric recovery under a fixed page TTL.
See `docs/M93_FULL_PAGE_DEPLETION_RECOVERY.md`.

M94 leaves the empty target page idle under Aero's normal default 600-frame
TTL, proves its cache expiration, then restores the page through direct-one
and rebuilt-two-member states. See `docs/M94_DEFAULT_TTL_PAGE_RECOVERY.md`.

M95 constrains M78's exact four requested pages to a three-entry cache. Every
retained record rebuilds and evicts exactly two pages while preserving sixteen
enqueues, four page calls, and zero direct fallback. See
`docs/M95_PAGE_CAPACITY_THRASH.md`.

M96 lowers that exact fixture to capacity two and exposes bounded tie behavior:
fresh JVMs select rebuild mode three or four, while every record couples its
capacity-eviction delta exactly to the rebuild count. See
`docs/M96_PAGE_CAPACITY_TWO_THRASH.md`.

M97 lowers the same fixture to one cache entry. Every retained record rebuilds
and evicts all four pages, with zero direct fallback and no capacity-two tie
mode. See `docs/M97_PAGE_CAPACITY_ONE_THRASH.md`.

M98 sets the configured maximum to zero but proves the protected compiled page
still leaves an effective cache floor of one; all four pages rebuild/evict per
record. See `docs/M98_ZERO_CAPACITY_FLOOR.md`.

M99 returns to a one-entry cache and limits page rebuilds to two per frame.
Every retained record compiles two pages, renders four remaining instances by
direct fallback, and advances capacity evictions by two. See
`docs/M99_REBUILD_BUDGET_FALLBACK.md`.

M100 lowers that budget to one and proves a strict per-frame alternation:
`pageCalls2/direct4` then `pageCalls1/direct10`, always with rebuild1, cache1,
and eviction delta one. See `docs/M100_REBUILD_BUDGET_ONE_FALLBACK.md`.

M101 lowers the rebuild budget to zero while retaining cache maximum one.
Every retained record stays cache-empty and renders all sixteen synchronized
instances directly, with zero page calls, rebuilds, and evictions. See
`docs/M101_REBUILD_BUDGET_ZERO_DIRECT.md`.

M102 passes the literal negative-one rebuild sentinel and proves the pinned
unlimited gate: all four page keys rebuild, direct fallback stays zero, and
capacity evictions advance by four per retained record. See
`docs/M102_UNLIMITED_REBUILD_SENTINEL.md`.

M103 disables cell pages explicitly and proves every queue entry draws through
the immediate direct overload. A smoke-owned counter and the M74 census expose
the work that later empty flushes erase from public cell counters. See
`docs/M103_PAGES_DISABLED_IMMEDIATE_DIRECT.md`.

M104 directly pairs pages enabled and disabled under the same sixteen-entity
plan, nonce, camera, and recorder schema in balanced fresh-process order. It
proves both structural paths without treating timing deltas as causal. See
`docs/M104_PAIRED_PAGES_CONTROL.md`.

M105 pairs capacity one with the literal negative-one unlimited page cache.
The same stable four-page scene either rebuilds/evicts continuously or retains
all four warmed pages without rebuilds. See
`docs/M105_PAIRED_CACHE_CAPACITY.md`.

M106 pairs page minimums two and five under the same exact scene. Raising the
minimum preserves two cached pages while routing four low-population instances
through the direct fallback, with independent M74 counter agreement. See
`docs/M106_PAIRED_MINIMUM_INSTANCES.md`.

M107 pairs the pinned skip-individual switch under minimum2. The enabled arm
queues all sixteen identities through the managed pre-dispatch hook with no
individual renderer calls; the disabled arm reaches the same four cached pages
through sixteen manual renderer calls. See
`docs/M107_PAIRED_SKIP_INDIVIDUAL.md`.

M108 pairs pinned cell sizes two and eight under the same fixed aligned scene.
Size two produces four cached page calls; size eight produces one, while both
retain sixteen queue/renderer calls and no fallback or rebuild. See
`docs/M108_PAIRED_CELL_SIZE.md`.

M109 pairs raw cell-size zero with the explicit supported floor one. Both
resolve to effective size one and reproduce sixteen single-member direct
fallbacks with no cached pages. See `docs/M109_CELL_SIZE_FLOOR.md`.

M110 pairs raw cell-size thirty-three with the explicit ceiling thirty-two.
Both resolve to effective size thirty-two and reproduce one cached page holding
all sixteen identities. See `docs/M110_CELL_SIZE_CEILING.md`.

M111 returns the active roadmap to vanilla Minecraft. Two fresh official
Beta 1.7.3 worlds with the fixed Worldline seed expose the same absolute
chunk `(0,0)`: 16,342 non-air blocks, an identical complete ID volume, and an
identical 256-column top-Y/ID/metadata surface profile. Player spawn remains
observational. See `docs/M111_FIXED_SEED_TERRAIN.md`.

M112 qualifies the two complete Packet51 light planes for that same world
boundary. Two fresh official worlds reproduce exact block-light and sky-light
hashes and their complete 0–15 histograms. This is a snapshot oracle; source
attribution and light-engine transitions remain open. See
`docs/M112_FIXED_SEED_LIGHTING.md`.

M113 performs the first causal vanilla light transition. An official
Packet15 placement adds one glowstone source; after a bounded update window, a
fresh session's Packet51 contains exactly 68 increased block-light samples,
level 15 at the source, and no sky-light changes. See
`docs/M113_CAUSAL_LIGHTING.md`.

M114 opens one dirt cell directly below naturally generated still water. The
official server first confirms air, then settles the cell to water `9:8` in
both the live cache and a fresh Packet51, with exactly one full-chunk state
change. See `docs/M114_CAUSAL_WATER_FLOW.md`.

M115 adds bounded empty-hand block activation and applies it to an official
lever. After a stabilized server-authored fixture, Packet15 changes `69:1` to
`69:9`; Packet53 and a fresh Packet51 prove exactly one chunk-state delta.
See `docs/M115_LEVER_ACTIVATION.md`.

M116 adds generic held-item-on-block use for the positive protocol item range
and places dust 331 as wire 55. Activating the adjacent lever changes the wire
from power zero to fifteen; exactly the lever and wire states differ in the
live and fresh-session world views. See `docs/M116_REDSTONE_WIRE_POWER.md`.

M117 closes that first redstone cycle. From the exact powered lever/wire pair,
a second official activation returns both states to their off/zero values;
Packet53, fresh Packet51 and the two-state chunk delta all agree. See
`docs/M117_REDSTONE_WIRE_DEPOWER.md`.

M118 adds the first powered consumer. Item 330 creates the two-block iron door,
and the adjacent lever changes its lower and upper metadata from `0/8` to
`4/12`; exactly those halves and the lever differ. See
`docs/M118_REDSTONE_IRON_DOOR.md`.

M119 adds the first block-gravity outcome. Removing one stone support beneath
sand moves `12:0` down exactly one cell; the live cache, fresh Packet51 and
two-state chunk delta agree. See `docs/M119_FALLING_SAND.md`.

M120 adds bounded horizontal fluid propagation. A sealed official stone trench
holds source `9:0` behind dirt; opening its only exit produces target `9:1`
with exactly one authoritative chunk-state change. See
`docs/M120_HORIZONTAL_WATER.md`.

M121 expands fixed-seed world observation to a complete 3x3 region after clean
restart. Two fresh replicas match the aggregate solid count, exact 2,304-column
surface and solid occupancy across every internal chunk seam. Interior masks,
IDs and metadata remain explicit diagnostics rather than a false determinism
claim. See `docs/M121_FIXED_SEED_REGION.md`.

M122 samples every block-light and sky-light nibble across the same nine chunks
after clean restart. Two fresh replicas match both 294,912-sample plane hashes
and all histogram bins without normalization. See
`docs/M122_FIXED_SEED_REGION_LIGHTING.md`.

M123 places one official glowstone at global edge coordinate `x=15` and proves
that block light propagates into water in chunk `x=1`: source `0→15`, neighbor
`0→12`, with exact deltas in both chunks and no skylight change. See
`docs/M123_CROSS_CHUNK_LIGHTING.md`.

M124 removes that exact edge source, observes water `9:0` return, and proves
both complete block-light planes recover byte-for-byte to their baselines. See
`docs/M124_CROSS_CHUNK_LIGHT_RECOVERY.md`.

M125 opens a bounded trench from source water at global `x=15` into chunk 1.
The source chunk remains unchanged while exactly one neighbor state becomes
water `9:1`. See `docs/M125_CROSS_CHUNK_WATER.md`.

M126 activates a lever in chunk 1 while its attached support and redstone wire
remain in chunk 0. The official server changes the lever from metadata `1` to
`9` and the cross-seam wire from power `0` to `15`. See
`docs/M126_CROSS_CHUNK_REDSTONE.md`.

M127 deactivates the same cross-seam signal through a fresh client. Lever and
wire return to metadata `1` and `0`, and both complete chunks exactly match the
original unpowered snapshots. See
`docs/M127_CROSS_CHUNK_REDSTONE_RECOVERY.md`.

M128 composes a lever in chunk 1 with a two-block iron door in chunk 0. One
activation produces the exact lever delta and both official door-half deltas.
See `docs/M128_CROSS_CHUNK_IRON_DOOR.md`.

M129 closes the same door through a fresh client and proves exact inverse
deltas plus state-for-state recovery of both complete chunks. See
`docs/M129_CROSS_CHUNK_IRON_DOOR_RECOVERY.md`.

M130 adds an opt-in official Nether server profile and dimension-aware player
seed. A player logs into dimension `-1`, decodes exact Nether terrain and saves
back with `Dimension=-1`. See `docs/M130_NETHER_LOGIN.md`.

M131 exposes typed Packet1/Packet9 dimension state, runs simultaneous Overworld
and Nether clients, and invalidates old chunks on a real dimension change. See
`docs/M131_DUAL_DIMENSION_SESSION.md`.

M132 constructs a complete 14-obsidian frame through ordinary protocol-14
placement, ignites it with flint and steel, and proves six persisted official
portal blocks through a fresh client. See `docs/M132_NETHER_PORTAL_ACTIVATION.md`.

M133 enters that active portal, observes the official Packet9 transition from
dimension `0` to `-1`, decodes the generated Nether-side portal and persists
the new player dimension. See `docs/M133_NETHER_PORTAL_TRAVERSAL.md`.

M134 discovers the generated portal, leaves it for a bounded cooldown and
re-enters to prove Packet9 `-1→0`, cache replacement, a valid Overworld portal
and final dimension-`0` persistence. See `docs/M134_NETHER_PORTAL_ROUNDTRIP.md`.

## Verify

Requirements:

- JDK 21 for the repository harness;
- `tokei` 14 or newer for per-file source limits.

Run the canonical gate from the repository root:

```text
java tools/harness/Verify.java
```

The gate checks per-file source limits, compiles product modules to
Java 8 bytecode in their declared dependency order, compiles the tests, and
runs every registered test suite. Derived output is written to the ignored
`.worldline/build/` directory.

Runtime-bound work uses the stricter profile:

```text
java tools/harness/Verify.java --runtime
```

It additionally requires both official b1.7.3 client and dedicated-server JARs
under `local/artifacts/` and verifies their byte lengths, SHA-1, and SHA-256
against committed descriptors. The descriptors are metadata; the JARs remain
ignored and local. Acquire either or both frozen artifacts with:

```text
java tools/artifacts/Acquire.java client
java tools/artifacts/Acquire.java server
java tools/artifacts/Acquire.java all
```

Bootstrap the pinned RetroMCP upstream checkout and CLI under `local/` with:

```text
java tools/toolchains/Bootstrap.java retromcp
```

The bootstrap refuses modified or foreign checkouts, detaches at the committed
revision, and builds the CLI with the upstream Gradle wrapper. Once a clean
pinned checkout and non-empty CLI exist, repeated runs are local no-ops.

Run the complete evidence suite end to end with:

```text
java tools/harness/Verify.java --smoke
```

The command prepares the frozen b1.7.3 RetroMCP workspace and verifies both
levels of evidence. The original server smoke proves an eight-tick
`World.tick()` fixture. The completion smoke decompiles and recompiles the
client when necessary, calls `bootHeadless()`, `loadWorld(...)`, and `tick(1)`,
and verifies the compiled path reaches mapped `Minecraft.runTick()`. A separate
oracle compiled directly against the hash-verified official client JAR reaches
`net.minecraft.client.Minecraft.k()`. Two fresh JVMs per side must be
internally deterministic, match across the differential boundary, and equal
the committed SHA-256 signature. See
`smokes/controlled-client-tick/MAP.md` for the exact symbol map, external
boundary inventory, headless substitutions, and pass conditions.

M2 promotes those process boundaries to a stable milestone: virtual clock,
programmable input, RNG reseed, filesystem journal/failure injection, offline
network, tick scheduler, and timer-thread supervision. The public product
version stays 0.7.0 / M9; the frozen evidence is the same 16-tick state
signature. See `docs/M2_RUNTIME.md` and `docs/M2_CYCLE.md`.

The gate next runs `smokes/m3-domain-api`. Two fresh JVMs exercise the stable,
neutral Worldline API while two independent JVMs perform the equivalent
operations directly against the official obfuscated client JAR. All four must
produce the frozen M3 trace and signature documented in
`smokes/m3-domain-api/MAP.md`.

`smokes/m4-durable-snapshot` then captures the same logical state in two fresh
JVMs, requires byte-identical snapshot artifacts, restores each in new JVMs,
matches the direct official-client state at tick 4, and proves checksum failure
on a corrupted artifact plus explicit rejection of unknown versions and runtime
identities. Its scope is defined in
`smokes/m4-durable-snapshot/MAP.md`.

`smokes/m5-reproduction-bundle` next produces byte-identical bundles in two
fresh pack JVMs, replays the original and a copied artifact through the public
CLI, matches the direct official-client state, and rejects corruption plus
incompatible client and toolchain declarations. Its exact boundary is in
`smokes/m5-reproduction-bundle/MAP.md`.

`smokes/m6-trace-explorer` independently reruns the mapped and official clients,
views their equal 17-record traces, injects one `tick9.slot` change, and requires
the CLI to locate that exact first divergence in both comparison directions.
It also rejects a malformed schema; details are in
`smokes/m6-trace-explorer/MAP.md`.

`smokes/m7-mod-loading` then builds two descriptor-selected mods, executes their
distinct glass and gold effects through the real controlled client, and rejects
wrong runtime, API, entrypoint type, malformed, and missing descriptor cases.
Its exact evidence boundary is in `smokes/m7-mod-loading/MAP.md`.

`smokes/m8-mod-version-diff` executes a no-mod baseline and versions `1.0.0`
and `1.1.0` of the same mod twice each, freezes deterministic JAR/trace/result
hashes, and requires exact baseline/version and version/version divergences.
Its scope is in `smokes/m8-mod-version-diff/MAP.md`.

`smokes/m9-scenario-minimization` repeatedly reexecutes both M8 mod versions,
reduces nine noisy actions to three steps, and proves that removing any final
step changes or removes the exact divergence. Its evidence map is
`smokes/m9-scenario-minimization/MAP.md`.

`smokes/m10-native-render` then excludes all headless LWJGL stubs, creates a
real 64 by 64 Pbuffer, draws an exact quad through Minecraft's renderer, and
requires four-process mapped/official framebuffer equality. It also records
the Aero candidate as absent without claiming compatibility. Its boundary is
in `smokes/m10-native-render/MAP.md`.

`smokes/m11-aero-attribution` then pins and builds Aero Model Lib 3.0.0,
executes all 222 core tests, boots its StationAPI consumer, and qualifies the
neutral work-attribution boundary. `smokes/m12-aero-reproduction` creates two
controlled dense scenes, ingests their real frame logs, reproduces the
chunk-compilation spike, and minimizes each evidence window. Their exact
boundaries are in the corresponding `MAP.md` files.
`smokes/m13-aero-differential` then reloads the exact dense save, compares it
with an Aero-disabled world, and tests the compile governor.
`smokes/m14-chunk-backlog` instruments the non-forced caller and dirty queue,
then compares vanilla with a real two-rebuild, non-retry bounded policy.
`smokes/m15-chunk-contract` moves that experiment to an adapter-owned explicit
work result, measures dirty age and visible readiness, and compares exact chunk
vertex signatures. Comparative fixed-batch readiness is observational; the
policy remains experimental and temporal visual divergence remains unresolved.
`smokes/m16-adaptive-chunks` restores one canonical save into both modes,
applies visible-first adaptive work under a time envelope, freezes tick 20,
drains the global queue, and bounds differences across every RGBA pixel.
`smokes/m17-scheduler-hardening` extends that proof across three stationary and
moving scenarios, records governor backlog, adaptive overshoot and global
drainage, and fails promotion on the observed strict framebuffer divergences.
`smokes/m18-save-attribution` restores one dense save into skip and live
processes, injects one non-forced save, and freezes the colocated timeline
without claiming the historical spike is gone.
`smokes/m19-forced-autosave` forces a dirty set and proves the opt-in one-chunk
non-forced save cap lowers the worst synthetic batch while staying default-off.

`smokes/gui-tree` then opens, inspects, clicks, and closes the inventory screen
through the neutral `GameUi` tree and matches two official-JAR oracle processes.
This is a stable milestone; the public product version stays 0.7.0 / M9. See
`smokes/gui-tree/MAP.md` and `docs/GUI_TREE.md`.

The Invariant Engine observes item, block, entity, wear, health, and time
samples and fails closed when a rule is broken. The controlled client cycle
watches `standard(runtime)` on every live tick. Trace and mod-test diffs
name the matching rule for known conservation fields. See
`docs/INVARIANTS.md`.

The semantic catalog annotates the 24 controlled-boundary categories, both
`symbols.map` files, the adapter/oracle/item/recipe/domain surfaces Worldline
already executes, and the native autosave/chunk-save/compile-chunk symbols.
Adapter manifests bind Worldline-owned sites to those roles and reject Aero
types. A coverage gate and static role graph fail closed on unknown tokens
or unmapped names. Trace diffs print `role=` for known fields. See
`docs/SEMANTICS.md`.

The gate then runs `smokes/lab-cycle`, restores deterministic checkpoints in
fresh clients, compares hypotheses, exercises GUI selectors, and compiles and
loads `probe-mod.jar`; its scope is defined in `smokes/lab-cycle/MAP.md`.

The repository has no total line cap. Product files remain limited to 250 code
lines, harness files to 300, and smoke/oracle and game-specific adapter files
to 150. This preserves modular pressure without preventing the project from
growing through new cohesive files and modules.

See `ARCHITECTURE.md` for module boundaries and `AGENTS.md` for the behavioral
and engineering constitution. `FIRST_CYCLE.md` is the v0.0.1 GO audit;
`M2_CYCLE.md` is the controlled-runtime-boundary GO audit;
`LAB_CYCLE.md` is the seven-step laboratory GO audit.
`M3_CYCLE.md` is the v0.1.0 stable domain-API GO audit.
`M4_CYCLE.md` is the v0.2.0 durable-snapshot GO audit.
`M5_CYCLE.md` is the v0.3.0 reproduction-bundle and replay-CLI GO audit.
`M6_CYCLE.md` is the v0.4.0 trace-viewer and first-divergence GO audit.
`M7_CYCLE.md` is the v0.5.0 general-mod-loading and compatibility GO audit.
`M8_CYCLE.md` is the v0.6.0 differential-mod-testing GO audit.
`M9_CYCLE.md` is the v0.7.0 automatic-scenario-minimization GO audit.
`M10_CYCLE.md` is the v0.8.0 native/offscreen-render GO audit.
`M11_CYCLE.md` is the v0.9.0 Aero-attribution GO audit.
`M12_CYCLE.md` is the v1.0.0 real-Aero-reproduction GO audit.
`M13_CYCLE.md` is the v1.1.0 Aero persistence/differential GO audit.
`M14_CYCLE.md` is the v1.2.0 chunk-backlog/caller-policy GO audit.
`M15_CYCLE.md` is the v1.3.0 explicit-contract/readiness GO audit.
`M16_CYCLE.md` is the v1.4.0 adaptive-scheduler/framebuffer GO audit.
`M17_CYCLE.md` is the v1.5.0 matrix GO and scheduler-promotion NO-GO audit.
`M18_CYCLE.md` is the v1.6.0 save-attribution GO and historical-spike non-claim.
`M19_CYCLE.md` is the v1.7.0 forced-autosave and opt-in save-cap GO audit.
`M20_CYCLE.md` is the v1.8.0 official-server identity and lifecycle GO audit.
`M21_CYCLE.md` is the v1.9.0 dedicated-server command/save/state GO audit.
`M22_CYCLE.md` is the v1.10.0 protocol-14 multiplayer login/disconnect GO audit.
`M23_CYCLE.md` is the v1.11.0 persisted multiplayer player-state GO audit.
`M24_CYCLE.md` is the v1.12.0 multiplayer position/look exchange GO audit.
`M25_CYCLE.md` is the v1.13.0 persisted multiplayer movement GO audit.
`M26_CYCLE.md` is the v1.14.0 native multiplayer render-bridge GO audit.
`M27_CYCLE.md` is the v1.15.0 two-client native chat/pump GO audit.
`M28_CYCLE.md` is the v1.16.0 remote chunk-envelope observation GO audit.

`M29_CYCLE.md` is the v1.17.0 strict remote chunk-snapshot GO audit.

`M30_CYCLE.md` is the v1.18.0 bounded remote-world cache GO audit.

`M31_CYCLE.md` is the v1.19.0 server-authoritative incremental-world GO audit.

`M32_CYCLE.md` is the v1.20.0 sustained cache-to-native-render GO audit.

`M33_CYCLE.md` is the v1.21.0 chunk-traversal lifecycle GO audit.
`GUI_CYCLE.md` is the inventory Game UI tree GO audit.
`INVARIANTS_CYCLE.md` is the conservation-rule GO audit.
`SEMANTICS_CYCLE.md` is the catalog and role-graph GO audit.

After preparing the local runtime with the canonical smoke gate, replay a
bundle with:

```text
java tools/replay/Replay.java replay path/to/reproduction.wlrb
```

Inspect or compare canonical state traces without a Minecraft runtime:

```text
java tools/replay/Replay.java trace show run.wltrace
java tools/replay/Replay.java trace diff baseline.wltrace candidate.wltrace
```

Inspect a local Worldline mod package without executing it:

```text
java tools/replay/Replay.java mod inspect path/to/mod.jar
```

Record and compare provenance-bound mod test results:

```text
java tools/replay/Replay.java mod test record mod.jar run.wltrace run.wlmtest
java tools/replay/Replay.java mod test diff baseline.wlmtest candidate.wlmtest
```

Create or inspect a canonical scenario artifact:

```text
java tools/replay/Replay.java scenario create run.wlscenario tick observe:target
java tools/replay/Replay.java scenario inspect run.wlscenario
```

Version and frozen evidence are authoritative in
`release/worldline.properties`. See `CHANGELOG.md` for stable scope and
`docs/ROADMAP.md` for the distinction between official and experimental stages.

## Legal boundary

Do not place official JARs, original assets, or decompiled Minecraft source
in Git. Local artifacts and experiments belong under the ignored `local/`
directory. Public work should consist of original code, mappings, patches, and
transforms.
