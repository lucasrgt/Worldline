# Worldline Roadmap

The roadmap distinguishes completed evidence from stable public contracts.
Passing an experiment does not silently promote its API.

| Stage | Objective | Current state |
| --- | --- | --- |
| v0.0.1 | Headless b1.7.3 boot, world load, one manual client tick, official oracle match | GO - stable milestone |
| M2 | Virtual clock, input, RNG, filesystem, network, scheduler, and thread control | GO - stable milestone |
| v0.1.0 / M3 | Stable world, player, entity, and block automation API | GO - stable milestone |
| v0.2.0 / M4 | Durable snapshot format and cross-process restore | GO - stable milestone |
| v0.3.0 / M5 | Portable reproduction bundles and replay CLI | GO - stable milestone |
| v0.4.0 / M6 | Trace viewer and first-divergence explorer | GO - stable milestone |
| v0.5.0 / M7 | General mod loading and compatibility contracts | GO - stable milestone |
| v0.6.0 / M8 | Differential mod/version testing | GO - stable milestone |
| v0.7.0 / M9 | Automatic scenario minimization | GO - stable milestone |
| v0.8.0 / M10 | Native/offscreen render E2E and Aero investigation | GO - render stable; Aero artifact absent |
| v0.9.0 / M11 | Render-work attribution and exact Aero qualification | GO - bounded attribution and StationAPI load |
| v1.0.0 / M12 | Real Aero scene capture, repeated spike classification, and evidence minimization | GO - chunk-compilation spike reproduced; persistence split deferred |
| v1.1.0 / M13 | Aero persistence isolation, dense/empty differential, and compile-budget test | GO - real BEs persist; empty retains compile pressure; budget rejected |
| v1.2.0 / M14 | Chunk caller semantics, initial-backlog attribution, and bounded non-retry policy | GO - caller/backlog isolated; policy experimental |
| v1.3.0 / M15 | Explicit accepted/deferred contract, visible readiness, and geometry oracle | GO - contract qualified; fixed batch experimental |
| v1.4.0 / M16 | Visible-first adaptive chunk envelope and fixed-state framebuffer oracle | GO - scheduler qualified; post-overlay visual divergence |
| v1.5.0 / M17 | Three-scenario scheduler hardening and upstream evaluation profile | GO - matrix complete; promotion NO-GO |
| v1.6.0 / M18 | Save/GC/compile attribution of the historical random spike | GO - timeline colocated; spike NON-CLAIM |
| v1.7.0 / M19 | Forced dirty-set autosave and opt-in one-chunk save cap | GO - synthetic hitch bounded; cap default-off |
| v1.8.0 / M20 | Official b1.7.3 dedicated-server identity and lifecycle bootstrap | GO - two clean localhost boot/save/stop processes |
| v1.9.0 / M21 | Neutral dedicated-server command, save, and persisted-state control | GO - two official controller/server pairs |
| GUI tree | Neutral inventory Game UI tree with official-JAR match | GO - stable milestone |
| Invariant engine | Six fail-closed rules on live `watch(standard(runtime))` | GO - stable milestone |
| Semantic mappings | Closed 24-category catalog, adapter manifests, and static role graph | GO - stable milestone |

## Promotion rule

A stage becomes official only when its contract, non-claims, executable oracle,
frozen evidence, source provenance, and canonical gate are all committed to the
repository. The official Minecraft Beta 1.7.3 JAR remains local and is never a
release artifact.

## Immediate post-v1.9.0 direction

The next milestone boots one official client against one isolated localhost
server and controls login, player observation, command/save, and clean
disconnect. A later instrumentation milestone can pause or externally step the
server tick thread; M21 deliberately does not infer tick control from console
commands. The save-path fat-chunk/NBT question and M17 visual divergences remain
open independent research lines.
