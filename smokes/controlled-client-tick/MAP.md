# Controlled Client Tick Map

## Claim

Worldline v0.0.1 can construct the real Minecraft Beta 1.7.3 client tick
object graph without a window, load a deterministic in-memory world, and
advance externally requested ticks through `MinecraftRuntime.tick(int)`. The
mapped/recompiled client executes
`Minecraft.runTick()`. A separately compiled oracle executes its official JAR
counterpart, `net.minecraft.client.Minecraft.k()`. Their canonical observable
state is identical through 16 ticks in two fresh JVMs per side.

This is the completion proof for the first controlled-runtime cycle. It covers
one deterministic fixture and the observed state below; it does not claim that
every client feature or arbitrary world is deterministic.

## Frozen inputs

| Input | Frozen evidence |
| --- | --- |
| Official b1.7.3 client JAR | SHA-256 `af1fa04b8006d3ef78c7e24f8de4aa56f439a74d7f314827529062d5bab6db4c` |
| RetroMCP | Git `9ece383d9bfe993d763d75b503e913f0dfbd8852` |
| Version metadata | SHA-256 `be64dfdab54d85c986140b39345c2abc4fb05ad1dc567041a801e8af64f63944` |
| Tiny mappings | SHA-256 `c170d1fde77fccc36649fb2e09066e04670c835e00c0b46fcf6312c9560453a5` |
| Exceptions | SHA-256 `e02f5e01e0e0de6a4223958ce260ee4b4d5afa830a007a6a861afb55efda039d` |

The canonical gate verifies every hash before client execution. If mapped
client bytecode is absent, it asks the pinned RetroMCP CLI to decompile and
recompile the client, then requires the resulting
`net/minecraft/client/Minecraft.class`.

## Exact tick root

The mapping namespace order is `named`, `client`, `server`. The complete
machine-checked surface is in `symbols.map`; its causal spine is:

| Named client symbol | Official client symbol | Role |
| --- | --- | --- |
| `Minecraft.ticksRan` | `Minecraft.V` | Loop-owned client tick counter advanced by the adapter |
| `Minecraft.runTick()V` | `Minecraft.k()V` | Exact client tick root |
| `World.updateEntities()V` | `fd.g()V` | Entity phase reached by `runTick` |
| `World.tick()V` | `fd.l()V` | World phase reached by `runTick` |
| `World.getWorldTime()J` | `fd.t()J` | Logical-clock observation proving one world tick |
| `RenderGlobal.updateClouds()V` | `n.d()V` | Non-world client phase reached before the world tick |

The runner inspects compiled bytecode with `javap`. It fails unless the driver
calls `MinecraftRuntime.tick(I)`, the backend calls the named
`Minecraft.runTick()`, and the oracle calls official `Minecraft.k()`.
The same check requires mapped and official movement plus `TextureCompassFX`
roots. Four fresh compass texture arms cross east/west spawn positions with
yaw `0`/`180`, then compare smoothing state and the complete pixel digest.
Each process also reports the code source selected for `Minecraft`: the mapped
generated `instrumented-client/` directory for the subject and the
hash-verified official JAR for the oracle. The instrumented class differs only
at the checked virtual-clock hook and two decompiler-only redundant casts.

## Headless object graph

`bootHeadless()` invokes the original `Minecraft` constructor and installs the
minimum real vanilla collaborators used by `runTick`: `GameSettings`,
`GuiIngame`, `PlayerController`, `EntityRenderer`, `RenderEngine`,
`RenderGlobal`, and `StatFileWriter`. Constructor-free allocation is limited to
two classes whose constructors cross an excluded boundary: `RenderGlobal`
would build OpenGL display lists, and `StatFileWriter` would create files and a
background synchronizer. Their tick-reached methods remain the original or an
explicit neutral boundary method as listed below.

`loadWorld()` constructs the original client `World`, preloads a 5x5 flat
chunk fixture through vanilla `Chunk`, creates the original `EntityPlayerSP`
as the camera/input context, and creates the original `EffectRenderer`. The
player is intentionally not registered in `World.playerEntities`; this keeps
the first-cycle fixture free of nondeterministic creature spawning while still
exercising the complete client tick root.

## External-boundary inventory

| Boundary | First-cycle control |
| --- | --- |
| Window / LWJGL | Classpath-first original stubs keep `Display.isCreated()` false. `GL11.glBindTexture` and texture-name allocation are neutral no-ops. No native display is loaded or created. |
| Keyboard and mouse | Classpath-first programmable queues preserve vanilla event-loop semantics. Tick 2 injects the same key events into subject and oracle. |
| Textures | A `RenderEngine` subclass preserves construction but returns texture ID zero, neutralizes image upload, and makes dynamic texture upload a no-op. |
| Filesystem / world persistence | Original `World` uses an in-memory `ISaveHandler` and `IChunkLoader`; operations are journaled and support deterministic one-shot failure injection. |
| Filesystem / statistics | `StatFileWriter` is allocated without its filesystem constructor; its per-tick sync and write methods are neutral no-ops. |
| Clock | Exactly seven mapped client calls are transformed to `B173ClockHooks`; the virtual clock advances 50 ms per controlled tick. The default hook remains real wall time outside controlled mode. |
| RNG | Vanilla RNG implementations remain, but controlled world and player streams are explicitly reseeded. The seed is part of the v2 trace. |
| Networking | An offline `Session` is installed. No client network handler, socket, resource downloader, or server is constructed. |
| Audio | The vanilla sound manager exists but is never initialized with an audio backend and no sound path is reached by this fixture. |
| Threading | The original constructor's daemon timer-hack thread is captured, observed alive during execution, interrupted, joined, and observed stopped on close. |

M2 promotes this inventory to a stable milestone. The contract and non-claims
are in `docs/M2_RUNTIME.md`; the GO audit is `docs/M2_CYCLE.md`.

Every substitution is original Worldline smoke code and is applied identically
to the named subject and official oracle. No decompiled or proprietary source
is tracked.

## Canonical observation and pass condition

The v1 trace records load and the first controlled tick. The v2 trace records
load and all 16 ticks, including:

- vanilla world time;
- vanilla loaded-entity count;
- the private client tick counter;
- stone and air block IDs at the fixture column.
- RNG seed, player position/health/hotbar slot, GUI counter, renderer counter,
  and cloud counter.

Four separate JVMs must satisfy object-graph assertions, remain headless, be
pairwise deterministic, match across the mapped/official boundary, and equal
the frozen signature
`ac13115a73408c85eb80b931dc3004b4fd66b26a5512e8d4fb036eebf70ae780`.
The resulting trace proves both counters move from zero to one while the
fixture remains unchanged:

```text
v1|seed=17320110707|loaded:time=0,entities=0,column=0.1.0|tick1:time=1,entities=0,column=1.1.0
```

The v1 signature remains frozen. The expanded v2 signature, including the
tick-2 scheduled input, is
`e8cdeba39a44b772a70c48c0acd9ae3983f3d95a8c10c545df5d66fb953db554`.
