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
```

The modules are physical source roots and are compiled separately. The API is
compiled with no product classpath. The kernel is compiled with only the API on
its classpath. Reproduction depends only on the API; CLI depends only on API
and the stable product modules it exposes. Analysis depends only on trace.
This makes every declared dependency direction executable rather than
conventional.

### `api`

Owns stable concepts visible to a scenario author. It must not expose RetroMCP,
LWJGL, mappings, instrumentation, or decompiled Minecraft types.

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

### `kernel`

Owns control-flow policy: valid lifecycle transitions and the narrow backend
port that each Minecraft integration must implement. It must not claim that
a transition succeeded unless the backend call completed.

Kernel unit tests use a recording fake. The server smoke supplies a test-scoped
`VanillaWorldBackend` for direct world-level evidence. The client cycles use the
reusable `B173Runtime` adapter, which constructs the original client tick object
graph and reaches `Minecraft.runTick()` through the same port. The adapter has
its own enforced source budget and compiles against local, ignored Minecraft
classes; no game binary or decompiled source enters the repository.

### `trace`

Owns the versioned canonical trace grammar, input validation, SHA-256 signature,
and output framing shared by a subject and its oracle. It has no dependencies
on the API, kernel, RetroMCP, or Minecraft. Adapters collect observations; the
trace module only serializes supplied values and therefore cannot make both
sides agree on game behavior by itself.

## Artifact and toolchain provenance

`artifacts/` contains public descriptors, never game binaries. The b1.7.3
descriptor freezes Mojang's byte length and SHA-1 plus the observed SHA-256.
Runtime verification accepts only the matching local JAR under the ignored
`local/artifacts/` root.

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

`smokes/controlled-client-tick/` completes the client-level cycle. It invokes
the original `Minecraft` constructor, installs explicit headless boundaries,
loads an original client `World`, and executes exactly one externally requested
`Minecraft.runTick()`. Its independent oracle uses official client names and
the frozen official JAR. The runner checks class origins, bytecode call paths,
four-process determinism, cross-boundary equality, and a frozen trace. The exact
scope and non-claims are normative in that smoke's `MAP.md`.

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

An adapter may depend on the API and kernel. The API and kernel must never
depend on an adapter. The reusable client adapter is an executable proof of
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
