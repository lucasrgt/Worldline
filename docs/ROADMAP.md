# Worldline Roadmap

The roadmap distinguishes completed evidence from stable public contracts.
Passing an experiment does not silently promote its API.

| Stage | Objective | Current state |
| --- | --- | --- |
| v0.0.1 | Headless b1.7.3 boot, world load, one manual client tick, official oracle match | GO - stable milestone |
| M2 | Virtual clock, input, RNG, filesystem, network, scheduler, and thread control | Experimental evidence passes |
| v0.1.0 / M3 | Stable world, player, entity, and block automation API | GO - stable milestone |
| v0.2.0 / M4 | Durable snapshot format and cross-process restore | GO - stable milestone |
| v0.3.0 / M5 | Portable reproduction bundles and replay CLI | GO - stable milestone |
| v0.4.0 / M6 | Trace viewer and first-divergence explorer | GO - stable milestone |
| v0.5.0 / M7 | General mod loading and compatibility contracts | GO - stable milestone |
| v0.6.0 / M8 | Differential mod/version testing | GO - stable milestone |
| v0.7.0 / M9 | Automatic scenario minimization | GO - stable milestone |
| v0.8.0 / M10 | Native/offscreen render E2E and Aero investigation | GO - render stable; Aero artifact absent |
| v0.9.0 / M11 | Render-work attribution and exact Aero qualification | GO - bounded attribution and StationAPI load |
| v1.0.0 / M12 | Real Aero scene capture, repeated spike classification, and evidence minimization | GO - chunk-compilation spike reproduced; save reload remains open |

## Promotion rule

A stage becomes official only when its contract, non-claims, executable oracle,
frozen evidence, source provenance, and canonical gate are all committed to the
repository. The official Minecraft Beta 1.7.3 JAR remains local and is never a
release artifact.

## Immediate post-v1.0.0 direction

M13 should repair or isolate the Aero test fixture's BlockEntity persistence,
compare the dense scene with an empty/no-Aero scene under the same chunk path,
and then test chunk-compile budgeting as a hypothesis. M12 identifies a
repeatable bounded-fixture cause; it does not equate it with the historical
random spike without that differential evidence.
