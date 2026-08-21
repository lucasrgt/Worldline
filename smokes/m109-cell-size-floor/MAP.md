# M109 behavior map

M109 compares raw cell-size properties zero and one while retaining the same
sixteen synchronized identities, fixed plan/nonce, camera, and primitive
recorder. The pinned index clamps zero to its supported lower bound one.

At effective size one every identity occupies a distinct cell. With minimum
population two, neither raw arm builds a page: both preserve sixteen queue and
renderer calls, route sixteen identities through the direct fallback, and keep
the page cache empty. Independent M74 render/list counters remain `16/16`.

Both arms keep pages enabled, skip-individual false, unlimited cache and
rebuild budget, and TTL100000. Every aligned record must contain two flushes,
zero page calls, rebuilds, evictions, or immediate calls.

Frozen trace:

```text
v1|design=2-balanced-pairs-raw0-raw1+raw1-raw0-same-plan-nonce|fixture=constant16-synced+client-marker+exact-camera+plan2/80/0|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|config=pagesTrue+min2+skipFalse+cacheNegative1+rebuildNegative1+ttl100000|raw0=effectiveSize1+queue16+rendererCalls16+flush2+pageCalls0+direct16+rebuild0+cached0+M74render16/list16|raw1=effectiveSize1+queue16+rendererCalls16+flush2+pageCalls0+direct16+rebuild0+cached0+M74render16/list16|both=immediate0+evicted0|window=M74-min720intervals+12s|capture=unified-60byte-sidecar+same-index-census|stats=paired-descriptive-dynamic|generic-clamp-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `d5ba4fa589d791959dca34158989889ea9d5c29942b6bc44fca7a18bb800a69e`.

Timing values and directions remain descriptive and dynamic. M109 does not
claim generic clamping, configuration quality, memory cost, causality,
regression, improvement, inference, pixels, or historical lag reproduction.
