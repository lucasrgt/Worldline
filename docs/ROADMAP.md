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
| GUI tree | Neutral inventory Game UI tree with official-JAR match | GO - stable milestone |
| Invariant engine | Six fail-closed rules on live `watch(standard(runtime))` | GO - stable milestone |
| Semantic mappings | Closed 24-category catalog, adapter manifests, and static role graph | GO - stable milestone |
| M10 | Native/offscreen render E2E and Aero investigation | Not started |
| M11 | Mod API v2: lifecycle hooks, domain handles, scheduling, spawn/give surface | GO - stable milestone |
| M12 | One-command attested mod test runs (`mod test run`) | GO - stable milestone |
| M13 | Multi-mod dependency graphs with deterministic ordering | GO - stable milestone |
| M14 | Public scenario DSL with validated, runnable reproducers | GO - stable milestone |
| Pre-push gate | Versioned hook running the canonical gate before every push | GO - stable milestone |
| M15 | Deterministic differential fuzzer with auto-minimized findings | GO - stable milestone |
| M16 | Time-travel debug REPL with deterministic reverse jumps and watchpoints | GO - stable milestone |
| M17 | Per-tick profiling with machine-relative budget gates | GO - stable milestone |
| M18 | Dynamic scenario coverage against the semantic catalog | GO - stable milestone |

## Promotion rule

A stage becomes official only when its contract, non-claims, executable oracle,
frozen evidence, source provenance, and canonical gate are all committed to the
repository. The official Minecraft Beta 1.7.3 JAR remains local and is never a
release artifact.

## Immediate post-v0.7.0 direction

M10 should qualify native/offscreen rendering and the original Aero candidate
without weakening the headless, provenance, or differential boundaries.

The ecosystem line (M11-M14) turns the laboratory into a platform: mods gain
the full controlled domain surface, one command attests their behavior,
multiple mods compose deterministically, and reproducers become shareable
runnable artifacts. The next ecosystem steps are a published Maven/Gradle
TestKit for external authors and promotion of the remaining experimental
world-surface observations.

The dev-tools line (M15-M16) composes those pieces into automatic loops:
fuzzing that shrinks its own findings and deterministic time travel over any
scenario. The next dev-tools steps are tick profiling with regression budgets
wired to the optimization SDK and runtime coverage against the semantic
catalog.
