# M15 Explicit Chunk-Work Contract

M15 separates two conclusions that M14 could not: whether deferral can be
represented safely and whether a fixed two-rebuild budget preserves visible
terrain readiness. The contract succeeds; the fixed policy does not.

## Explicit accepted/deferred result

The adapter-owned `Aero_ChunkWorkContract` returns `COMPLETE`,
`ACCEPTED_DEFERRED`, or `STALLED_DEFERRED` together with accepted and remaining
counts. The smoke redirects the sole `GameRenderer.renderFrame` call site,
maps accepted deferred work to end-of-current-frame, and resumes on the next
rendered frame. This is the distinction missing from vanilla's Boolean result.

Across the first 300 measured frames, baseline made 1,301 non-forced calls and
returned `false` every time. Contract mode made exactly 300 calls, accepted 600
real rebuilds, returned `true` 300 times, reported 300 accepted/deferred
outcomes, and never stalled. The same-frame retry loop is gone.

## Visible readiness and dirty age

Measurement starts at the first world-ready frame. Each frame samples the real
chunk array after culling, including dirty age, visible dirty chunks, and
visible built-and-clean chunks. Both queues began above 5,400 entries.

In the qualifying run, baseline reduced its queue from 5,405 to 4,441 and
visible dirty chunks from 1,268 to 831 in 300 frames, reaching 966 visible-ready
chunks. The fixed batch reduced its queue only from 5,406 to 4,867 and visible
dirty chunks from 1,268 to 1,222, reaching 541 visible-ready chunks. Both had
initial dirty entries aged 299 frames at that boundary.

The fixed batch therefore improves frame pacing only by deferring too much
visible work. It is rejected as an Aero mitigation despite removing retries.
Exploratory frame p95 was 20.7 ms baseline and 12.4 ms contract in this run;
timings are reported, not frozen.

## Chunk-geometry oracle

M15 hashes every submitted chunk vertex with exact position, UV, color,
normal, and layer-emptiness state. Across two qualifying repetitions, 670 and
851 common non-empty positions matched exactly, while 78 and 128 differed. The
slower queue rebuilds dynamically changing chunks at later world ticks, so the
ordering itself is not a stable initial cohort.

This proves broad exact per-chunk agreement while also exposing temporal
divergence. Full visual equivalence is not established; this is not a complete
framebuffer, transparency-order, HUD, or driver oracle.

## Next boundary

M16 should retain `ACCEPTED_DEFERRED` but choose work from visible debt and a
bounded time/accepted-work envelope. It must approach vanilla visible readiness
without restoring the same-frame retry loop, then extend visual evidence to a
fixed-tick framebuffer or equivalent render-pass oracle.
