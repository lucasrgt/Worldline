# M94 default-TTL page recovery

Status: GO in Worldline v1.82.0.

M94 repeats M93's exact six-member page depletion but leaves
`aero.becell.pageTtlFrames` and `aero.perf.memory` absent. Under the pinned Aero
revision this selects the normal default TTL of 600 frames. Runtime validation
fails if either property is present.

After membership reaches ten and the target page is empty, the other three
pages continue active flushes. The 128-flush sweeper expired exactly the target
entry: cache `4 -> 3`, `expiredCachedPages 0 -> 1`, and max-cache eviction
remained zero. Expiry records were 1815 and 2086 in the two fresh replicas.

Restoration waited thirty complete records after expiry. The first restored
member used direct fallback while cache stayed three; the second member
recompiled the page and restored cache four. Later members followed the
batched rebuild route already qualified by M93.

The values and event positions are descriptive. The result is specific to the
pinned normal-memory default and exact page/scene.

Nonclaims: high-memory or explicit TTLs, max-cache eviction, other pages or
geometries, concurrent mutation, persistence, uninstrumented cost, causal or
inferential performance claims, pixels, cross-machine generality, combat, or
historical lag reproduction.
