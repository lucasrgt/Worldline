# M15 Explicit Chunk-Work Contract

M15 separates two conclusions that M14 could not: whether deferral can be
represented safely and whether a fixed two-rebuild budget preserves visible
terrain readiness. The contract succeeds; comparative policy readiness is not
stable enough to freeze.

## Explicit accepted/deferred result

The adapter-owned `Aero_ChunkWorkContract` returns `COMPLETE`,
`ACCEPTED_DEFERRED`, or `STALLED_DEFERRED` together with accepted and remaining
counts. The smoke redirects the sole `GameRenderer.renderFrame` call site,
maps accepted deferred work to end-of-current-frame, and resumes on the next
rendered frame. This is the distinction missing from vanilla's Boolean result.

Across every qualifying first-300-frame window, contract mode makes exactly 300
calls, accepts 600 real rebuilds, returns `true` 300 times, reports 300
accepted/deferred outcomes, and never stalls. The same-frame retry loop is gone.

## Visible readiness and dirty age

Measurement starts at the first world-ready frame. Each frame samples the real
chunk array after culling, including dirty age, visible dirty chunks, and
visible built-and-clean chunks. Both queues began above 5,400 entries.

Queue direction, comparative readiness, and frame pacing vary with render
throughput and concurrent chunk discovery. An earlier qualifying run showed a
large fixed-batch readiness deficit; the post-M17 corrected-screen run did not.
Those comparisons are now printed observations rather than frozen promotion or
rejection criteria. The fixed batch remains experimental and is not promoted.

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

M16 retained `ACCEPTED_DEFERRED` while choosing work from visible debt and a
bounded time/accepted-work envelope. It must approach vanilla visible readiness
without restoring the same-frame retry loop, then extend visual evidence to a
fixed-tick framebuffer oracle. M17 later generalized that policy and rejected
promotion on broader readiness, overshoot, and visual evidence.
