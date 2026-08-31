# M780-AERO-SMOOTH-LIGHT-RESOLVED-CACHE Aero resolved smooth-light cache

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four fresh JVMs in ABBA order classify Aero's resolved smooth-light cache at its 50 ms default TTL using exact hashes of every resolved triangle brightness, diagnostic framebuffers, native hit and miss reasons, world-light work, render time, and hitches. Promotion requires render improvement in both rounds and at least five percent in aggregate; otherwise the evidence keeps the candidate disabled.

## Qualification cycle

M780 restores the same frozen world with 128 controlled dense smooth meshes into four fresh clients ordered cache-off, cache-on, cache-on, cache-off. Each 2,048-triangle mesh uses non-overlapping cells, opaque untextured emission, and a coordinate-sorted block-entity order. Every client pre-traverses 480 unmeasured frames, then executes a deterministic 240-frame orbit, traversal, spin, teleport, and close inspection. A scoped synthetic world-light grid drives the production smooth-light renderer. At the midpoint the grid changes while the camera is held fixed; before and after diagnostics bracket a 100 ms convergence window, exercising the cache's 50 ms stale-entry path. At 24 route checkpoints and both light diagnostics, the oracle hashes every resolved brightness value immediately before Aero emits it through tess.color and requires exact equality across all ABBA pairs. Complete RGBA frames remain captured and hashed as diagnostic artifacts but do not gate cache semantics because fresh OpenGL clients exhibit unrelated raster variation. The cycle also records render calls, world-light samples, native hit/miss reason counters, LRU state, render duration, and 50 ms hitches. It emits promote only when both rounds and the aggregate threshold win; all other performance outcomes keep the default disabled.

Expected signal: `scene=128-static-multichunk,jvms=4-fresh-abba,route=orbit+traverse+spin+teleport,light=phase-change+ttl-convergence,colors=0-resolved-differences,rgba=diagnostic,samples=reduced2of2,cache=hits+misses+cold+stale-censused,decision=promote-or-keep-disabled-by-render+hitches`.

Frozen semantic SHA-256: `36cb553c7a9d9a5cd946dfb801994449616c8db5e95931a669e7d649982249f7`.
