# ModLoader / Forge profiler driver

This driver uses the same Java 8-compatible `ClientProfilerRuntime`, metric
names, WLPR codec, budgets, and `mod.*` extension API as the StationAPI driver. The loader
difference is limited to interception: Beta 1.7.3 ModLoader and Forge projects
source-inject dependencies into RetroMCP and have no Mixin runtime.

Add the profiling module sources and `runtime-src` to the client source tree,
preserving their packages. Instrument these mapped vanilla boundaries with a
`try/finally` pair so every begin has exactly one end:

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
first captured frame.

This source boundary is structurally maintained. A dedicated official
ModLoader/Forge runtime qualification is still required before Worldline may
claim loader boot or performance equivalence; StationAPI qualification does not
transport across loaders.
