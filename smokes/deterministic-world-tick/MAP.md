<!-- worldline-map-schema=1 -->
<!-- boundary=server-runtime-equivalence -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=377497a2c5d84d3e10f98e14a1593d838d31373920a765f4d6360a4c1a0564f5 -->

# Deterministic World Tick Smoke Map

## Claim

The Worldline public runtime can drive a Minecraft Beta 1.7.3 server-side
vanilla `World` without a window or server socket. The mapped/recompiled world
is supplied with deterministic in-memory chunks and advanced for eight ticks
through `MinecraftRuntime.tick()`. The same fixture also executes directly
against the frozen official obfuscated server JAR. Two processes per path emit
the same canonical trace and signature.

This is a differential smoke test of a narrow controlled boundary. It proves
observable equivalence for this world fixture and eight-tick trace only. It does
not claim whole-game determinism, client determinism, snapshot support, or
equivalence outside the observed state.

## Frozen inputs

| Input | Frozen evidence |
| --- | --- |
| Client JAR | SHA-1 `43db9b498cb67058d2e12d394e6507722e71bb45` |
| Server JAR | SHA-256 `033a127e4a25a60b038f15369c89305a3d53752242a1cff11ae964954e79ba4d` |
| RetroMCP | Git `9ece383d9bfe993d763d75b503e913f0dfbd8852` |
| Version metadata | SHA-256 `be64dfdab54d85c986140b39345c2abc4fb05ad1dc567041a801e8af64f63944` |
| Tiny mappings | SHA-256 `c170d1fde77fccc36649fb2e09066e04670c835e00c0b46fcf6312c9560453a5` |
| Exceptions | SHA-256 `e02f5e01e0e0de6a4223958ce260ee4b4d5afa830a007a6a861afb55efda039d` |

## Symbol map

The mapping namespace order is `named`, `client`, `server`.
`symbols.map` is the declared machine-checked inventory of the vanilla surface
referenced by the smoke and official-oracle sources. Its named and server
columns define the two sides of the comparison; the table below highlights the
causal spine.

| Named symbol | Client | Server | Role in the smoke |
| --- | --- | --- | --- |
| `World` | `fd` | `dj` | Vanilla state container and tick implementation |
| `World.loadedEntityList` | `b` | `b` | Observable entity count |
| `World.rand` | `r` | `r` | Vanilla RNG seeded by the constructor path |
| `World.setBlockWithNotify(IIII)Z` | `f` | `e` | Places the sand block through vanilla notification logic |
| `World.tick()V` | `l` | `h` | Vanilla tick root exercised eight times |
| `World.getWorldTime()J` | `t` | `m` | Observable logical clock |
| `World.getChunkFromChunkCoords(II)` | `c` | `c` | Loads the deterministic in-memory fixture chunks |
| `IBlockAccess` | `xp` | `pb` | Contract that owns the mapped block lookup |
| `IBlockAccess.getBlockId(III)I` | `a` | `a` | Observes the canonical block column |
| `BlockSand` | `gk` | `ei` | Vanilla falling-block behavior |
| `BlockSand.fallInstantly` | `a` | `a` | Explicit test-mode scheduler boundary |
| `Block` | `uu` | `na` | Vanilla block registry and IDs |
| `Block.stone` / `sand` / `bedrock` | `u` / `F` / `A` | `u` / `F` / `A` | Fixture and expected result |
| `Chunk` | `lm` | `hi` | Vanilla chunk implementation populated by the loader |
| `WorldInfo` | `ei` | `ct` | Fixed seed, spawn, and logical time |
| `ISaveHandler` | `wt` | `om` | Replaced filesystem boundary |
| `IChunkLoader` | `bf` | `an` | Replaced chunk persistence/generation boundary |

## Controlled boundaries

- Seed: `17320110707`.
- Spawn: `(8, 64, 8)`.
- Chunks: a 5x5 preloaded square, bedrock at Y=0 and stone through Y=64.
- Persistence: in-memory no-op `ISaveHandler` and `IChunkLoader`.
- Falling-block scheduling: `BlockSand.fallInstantly = true`.
- Input, players, networking, display, audio, and filesystem writes: absent.

`WorldSource` supplies the scenario identity `memory/worldline-smoke`; the smoke
adapter uses its final path component as the vanilla world name. It is not a
filesystem input because persistence is replaced by the in-memory save handler.

## Executable control path

| Layer | Executed responsibility |
| --- | --- |
| Scenario | Calls only the public `MinecraftRuntime` lifecycle for boot, load, tick, and close |
| `ControlledMinecraftRuntime` | Enforces lifecycle state and delegates to `GameBackend` |
| `VanillaWorldBackend` | Implements the backend port using mapped b1.7.3 types |
| Vanilla `World` | Executes the real mapped `World.tick()` implementation |
| `CanonicalTrace` | Serializes supplied observations and emits the shared `v1` signature |

The runner inspects the compiled smoke bytecode with `javap` and fails unless
the scenario contains the `MinecraftRuntime.tick()` call and the adapter
contains the vanilla `World.tick()` call.

Everything after construction, block placement, and each call to `World.tick()`
is executed by the decompiled/recompiled vanilla server classes. The smoke trace
records world time, entity count, and block IDs from Y=64 through Y=70 before
placement, after placement, and after every tick.

On the Worldline side, named server classes are first on the runtime classpath.
The original server JAR follows them only to supply `lang/en_US.lang` and
achievement resources required by vanilla static initialization; its obfuscated
classes are not selected for named `net.minecraft.src.*` references.

The official oracle is separately compiled in the default package against the
dependency-free `worldline-trace` module and the frozen official server JAR. At
runtime its classpath contains the generated oracle classes, the neutral trace
classes, and that JAR; it contains no Worldline API, kernel, or
mapped/recompiled Minecraft classes. The oracle uses the frozen server names
from `symbols.map` and mirrors the same seed, chunks, block placement,
observations, assertions, and tick count.

Both sides supply independently collected time, entity count, and column values
to `CanonicalTrace`. Sharing serialization and SHA-256 code removes protocol
drift but does not share world construction, ticking, assertions, or observation
access.

## Pass condition

The runner starts four separate JVM processes: two Worldline executions and two
official-JAR oracle executions. They must:

1. satisfy the fixture and final-state assertions;
2. be internally deterministic within each pair;
3. emit byte-identical canonical traces across the Worldline/oracle boundary;
4. emit the SHA-256 signature committed in `smoke.properties`;
5. resolve every `symbols.map` entry inside the correct class block of the
   frozen `mappings.tiny`;
6. compile the official oracle directly against the hash-verified server JAR;
7. run with `java.awt.headless=true` and without binding a server socket;
8. preserve the compiled `MinecraftRuntime -> GameBackend -> World.tick` path;
9. use the same independently compiled canonical-trace module on both sides.

The frozen trace signature is
`377497a2c5d84d3e10f98e14a1593d838d31373920a765f4d6360a4c1a0564f5`.
