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

This source boundary is structurally maintained. A dedicated official
ModLoader/Forge runtime qualification is still required before Worldline may
claim loader boot or performance equivalence; StationAPI qualification does not
transport across loaders. That future receipt must name the concrete loader and
exact game/mod build rather than promoting this compile proof by inference.
