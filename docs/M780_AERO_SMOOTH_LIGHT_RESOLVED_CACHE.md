# M780-AERO-SMOOTH-LIGHT-RESOLVED-CACHE Aero resolved smooth-light cache

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Four fresh JVMs in ABBA order classify Aero's resolved smooth-light cache at its 50 ms default TTL using a fail-closed visual gate, native hit and miss reasons, world-light work, render time, and hitches. Promotion requires render improvement in both rounds and at least five percent in aggregate; otherwise the evidence keeps the candidate disabled.

## Qualification cycle

M780 restores the same frozen world with 128 controlled dense smooth meshes into four fresh clients ordered cache-off, cache-on, cache-on, cache-off. Each 2,048-triangle mesh uses non-overlapping cells. Immediately before the first controlled mesh each frame, the oracle clears color and depth so animated terrain and unrelated render passes cannot contaminate the subject framebuffer. Every client pre-traverses 480 unmeasured frames, then executes a deterministic 240-frame orbit, traversal, spin, teleport, and close inspection. A scoped synthetic world-light grid drives the production smooth-light renderer without changing unrelated terrain lighting. At the midpoint the grid changes while the camera is held fixed; before and after diagnostics bracket a 100 ms convergence window, exercising the cache's 50 ms stale-entry path. The cycle captures 24 route frames plus both light diagnostics, derives raster noise only from off/off and on/on repeats, forbids unexplained off/on pixels, and records render calls, world-light samples, native hit/miss reason counters, LRU state, render duration, and 50 ms hitches. It emits promote only when both rounds and the aggregate threshold win; all other performance outcomes keep the default disabled.

Expected signal: `scene=128-static-multichunk,jvms=4-fresh-abba,route=orbit+traverse+spin+teleport,light=phase-change+ttl-convergence,pixels=0-unexplained+noise<=10ppm,samples=reduced2of2,cache=hits+misses+cold+stale-censused,decision=promote-or-keep-disabled-by-render+hitches`.

Frozen semantic SHA-256: `7c6d1cfadd9a6a9df001f2fa4bc1aa99471ef7da69f02dde3402a0f8f494c463`.
