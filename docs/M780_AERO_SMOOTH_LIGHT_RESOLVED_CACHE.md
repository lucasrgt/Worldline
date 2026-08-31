# M780-AERO-SMOOTH-LIGHT-RESOLVED-CACHE Aero resolved smooth-light cache

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four fresh JVMs in ABBA order require Aero's resolved smooth-light cache at its 50 ms default TTL to preserve every observed framebuffer pixel outside independently measured same-arm raster noise before and after a real brightness-field change, refresh stale entries after the documented TTL, reduce world-light sampling and render time in both rounds, and add no material hitch regression.

## Qualification cycle

M780 restores the same frozen multi-chunk world with 128 controlled dense smooth meshes into four fresh clients ordered cache-off, cache-on, cache-on, cache-off. Each 2,048-triangle mesh uses non-overlapping cells to remove model z-fighting from the visual oracle. Every client pre-traverses 480 unmeasured frames, then executes a deterministic 240-frame orbit, traversal, spin, teleport, and close inspection. A scoped synthetic world-light grid drives the production smooth-light renderer without changing unrelated terrain lighting. At the midpoint the grid changes while the camera is held fixed; before and after diagnostics bracket a 100 ms convergence window, exercising the cache's 50 ms stale-entry path. The cycle captures 24 route frames plus both light diagnostics, derives raster noise only from off/off and on/on repeats, forbids unexplained off/on pixels, and records render calls, world-light samples, native hit/miss reason counters, LRU state, render duration, and 50 ms hitches.

Expected signal: `scene=128-static-multichunk,jvms=4-fresh-abba,route=orbit+traverse+spin+teleport,light=phase-change+ttl-convergence,pixels=0-unexplained+noise<=10ppm,samples+render=reduced2of2,cache=hits+misses+cold+stale-censused,hitches=censused`.

Frozen semantic SHA-256: `ed3c579c66b7c93f9bd6174d3ee8ddf987d0c6514096d45755e3ab047087092a`.
