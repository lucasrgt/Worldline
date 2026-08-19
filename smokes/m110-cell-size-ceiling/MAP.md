# M110 behavior map

M110 compares raw cell-size properties thirty-three and thirty-two while
retaining the same sixteen synchronized identities, fixed plan/nonce, camera,
and primitive recorder. The pinned index clamps thirty-three to its supported
upper bound thirty-two.

At effective size thirty-two the complete `(2,80,0)` wall occupies one cell.
Both raw arms preserve sixteen queue and renderer calls, one cached page call,
two flushes, and M74 render/list `0/0`, with no direct fallback, rebuild,
eviction, or immediate call.

Both arms keep pages enabled, minimum population two, skip-individual false,
unlimited cache and rebuild budget, and TTL100000. Every aligned record must
repeat the exact one-page structural state.

Frozen trace:

```text
v1|design=2-balanced-pairs-raw33-raw32+raw32-raw33-same-plan-nonce|fixture=constant16-synced+client-marker+exact-camera+plan2/80/0|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|config=pagesTrue+min2+skipFalse+cacheNegative1+rebuildNegative1+ttl100000|raw33=effectiveSize32+queue16+rendererCalls16+flush2+pageCalls1+direct0+rebuild0+cached1+M74render0/list0|raw32=effectiveSize32+queue16+rendererCalls16+flush2+pageCalls1+direct0+rebuild0+cached1+M74render0/list0|both=immediate0+evicted0|window=M74-min720intervals+12s|capture=unified-60byte-sidecar+same-index-census|stats=paired-descriptive-dynamic|generic-clamp-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `4061454ff65c9ef06366042094e79fc165c26e91d6f3af2fcd7f04638a180c0e`.

Timing values and directions remain descriptive and dynamic. M110 does not
claim generic clamping, configuration quality, memory cost, causality,
regression, improvement, inference, pixels, or historical lag reproduction.
