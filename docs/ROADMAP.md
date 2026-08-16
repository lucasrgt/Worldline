# Worldline Roadmap

The roadmap distinguishes completed evidence from stable public contracts.
Passing an experiment does not silently promote its API.

| Stage | Objective | Current state |
| --- | --- | --- |
| v0.0.1 | Headless b1.7.3 boot, world load, one manual client tick, official oracle match | GO - stable milestone |
| M2 | Virtual clock, input, RNG, filesystem, network, scheduler, and thread control | Experimental evidence passes |
| v0.1.0 / M3 | Stable world, player, entity, and block automation API | GO - stable milestone |
| v0.2.0 / M4 | Durable snapshot format and cross-process restore | GO - stable milestone |
| M5 | Portable reproduction bundles and replay CLI | Not started |
| M6 | Trace viewer and first-divergence explorer | Not started |
| M7 | General mod loading and compatibility contracts | Isolated probe seam passes |
| M8 | Differential mod/version testing | Not started |
| M9 | Automatic scenario minimization | Not started |
| M10 | Native/offscreen render E2E and Aero investigation | Not started |

## Promotion rule

A stage becomes official only when its contract, non-claims, executable oracle,
frozen evidence, source provenance, and canonical gate are all committed to the
repository. The official Minecraft Beta 1.7.3 JAR remains local and is never a
release artifact.

## Immediate post-v0.2.0 direction

M5 should package a snapshot with its declared runtime inputs into a portable
reproduction bundle and provide a replay CLI before the original Aero spike.
The current
`B173ModContext` remains an experiment seam, not an Aero compatibility claim.
