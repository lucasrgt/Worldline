# ModLoader / Forge profiler driver

This driver uses the same Java 8-compatible `ClientProfilerRuntime`, metric
names, WLPR codec, budgets, and `mod.*` extension API as the StationAPI driver. The loader
difference is limited to interception: Beta 1.7.3 ModLoader and Forge projects
source-inject dependencies into RetroMCP and have no Mixin runtime.

Run the fail-closed installer from the Worldline repository:

```text
java tools/integration/LegacyProfilerInstallerLauncher.java --check C:\path\to\retromcp modloader
java tools/integration/LegacyProfilerInstallerLauncher.java --install C:\path\to\retromcp modloader
```

Use `forge` as the last argument for a Forge workspace. The installer detects
`minecraft/src` or `src`, copies the exact Java 8 runtime manifest, creates a
local `.worldline-profiler/backup-v1`, and wraps these mapped vanilla boundaries
with `try/finally` so every begin has exactly one end:

| Mapped boundary | Hook pair |
| --- | --- |
| `Minecraft.runTick` | `tickBegin` / `tickEnd` |
| each `Display.update` in `Minecraft.run` | `displayBegin` / `displayEnd` |
| `EntityRenderer.updateCameraAndRender` | `frameBegin` / `frameEnd` |
| `EntityRenderer.renderWorld` | `worldBegin` / `worldEnd` |
| `RenderGlobal.updateRenderers` | `compileBegin(queueSize)` / `compileEnd` |
| `WorldRenderer.updateRenderer` | `rebuildBegin` / `rebuildEnd` |

Launch with `-Dworldline.profiler.enabled=true` and an absolute
`-Dworldline.profiler.output=...wlpr`. Set
`-Dworldline.profiler.loader=forge` for Forge; the default is `modloader`.
Mods register only owned `mod.*` metrics through `ClientProfiler` before the
first captured frame. The canonical portability check compiles this complete
runtime closure with `javac --release 8`.

Repeated installation is a verified no-op. Partial instrumentation, modified
installed sources, a conflicting runtime file, a mismatched loader receipt, or
an unrecognized decompile fails before another source is changed.

`loader.id` is descriptive metadata only: captures remain comparable across
loaders because metric identities, units, aggregation, and WLPR encoding are
owned by the shared profiling module.

## Controlled runtime qualification

The dedicated qualifier reconstructs both clients from the hash-pinned official
b1.7.3 JAR, overlays the exact historical loader archives, decompiles with
RetroMCP, installs this driver, recompiles to Java 8 bytecode, and starts each
client with a minimal loader-owned probe:

```text
java tools/integration/LegacyProfilerQualificationLauncher.java --qualify-all local/workspaces/b1.7.3 C:\path\to\loader-zips "C:\path\to\java8"
```

The artifact directory must contain `ModLoader B1.7.3.zip` and
`minecraftforge-client-1.0.7-20110907.zip`; their SHA-256 pins and the semantic
Forge version `1.0.6` live in `qualification.properties`. Generated clients are
isolated under `.worldline/runtime/legacy-profiler/workspaces` and never modify
the base workspace.

Qualification passes only when the concrete loader initializes the probe,
exactly eight frames seal into a checksum-valid WLPR carrying the expected
loader tags and metric capabilities, the probe requests `Minecraft.shutdown()`,
and the Java 8 process exits naturally with code zero. Local logs and sealed
receipts are written under `.worldline/reports/legacy-profiler`.

This proves loader boot, profiler capture, and clean shutdown for the pinned
minimal clients. It does not infer performance equivalence with StationAPI.

## TestKit providers

M767 adds the stable `modloader-b1.7.3` and `forge-b1.7.3` provider IDs. The
preparation command builds the two reusable local clients:

```text
java tools/integration/LegacyProfilerQualificationLauncher.java --prepare-testkit-all local/workspaces/b1.7.3 C:\path\to\loader-zips "C:\path\to\java8"
```

Each TestKit attempt uses a fresh data directory and starts one prepared Java 8
single-player client. The loopback protocol gates one world tick per command
and exposes lifecycle, world time, player identity, health, selected slot, and
pose. The service descriptor under `src/main/resources` publishes both
providers. M767 intentionally leaves mutation, GUI control, multiplayer parity,
and cross-loader performance parity unsupported.

The two `qualification-src` and two `testkit-src` probes are handwritten,
first-party fixtures whose package layout matches the legacy loader contract.
The release boundary allows only those four exact source names; compiled game
classes, decompiled sources, loader archives, and Minecraft artifacts remain
prohibited.
