# M108 behavior map

M108 compares the exact pinned cell-size literals two and eight while retaining
the same sixteen synchronized identities, paired plan/nonce, camera, and
primitive recorder.

The fixed aligned plan `(2,80,0)` spans two cells on Y and two on Z at size two,
but only one cell at size eight. Therefore size two replays four cached pages
of four members each, whereas size eight replays one cached page of sixteen.
Both retain all sixteen individual renderer calls because skip-individual is
false, while the independent M74 render/list counters remain `0/0`.

Both arms keep pages enabled, minimum population two, skip-individual false,
unlimited cache, unlimited rebuild budget, and TTL100000. Every aligned record
must contain sixteen queue entries and renderer calls, two flushes, no direct
fallback, rebuild, eviction, or immediate call.

Frozen trace:

```text
v1|design=2-balanced-pairs-size2-size8+size8-size2-fixed-aligned-plan-same-nonce|fixture=constant16-synced+client-marker+exact-camera+plan2/80/0|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|config=pagesTrue+min2+skipFalse+cacheNegative1+rebuildNegative1+ttl100000|size2=queue16+rendererCalls16+flush2+pageCalls4+direct0+rebuild0+cached4+M74render0/list0|size8=queue16+rendererCalls16+flush2+pageCalls1+direct0+rebuild0+cached1+M74render0/list0|both=immediate0+evicted0|window=M74-min720intervals+12s|capture=unified-60byte-sidecar+same-index-census|stats=paired-descriptive-dynamic|uninstrumented-cost-causality-better-size-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `7bd2dd0f5f557a19c07eaf9d79978bfbac81aee3ad313df51ac504740b7c303d`.

Timing values and their directions remain descriptive and dynamic. M108 does
not claim generic cell-size policy, cost attribution, causality, a better size,
regression, improvement, inference, pixels, or historical lag reproduction.
