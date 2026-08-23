<!-- worldline-map-schema=1 -->
<!-- boundary=aero-paired-experiment -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=913fff54f216f47e06d3886f94f4682b83c1d5bbf49648991c28926d71e8c6f3 -->

# M107 behavior map

M107 compares the pinned `aero.becell.skipIndividual` literals true and false
while retaining the same sixteen synchronized identities, four natural page
keys, minimum2, paired plan/nonce, camera, and primitive recorder.

With skip enabled, the fixture implements `Aero_CellPageRenderableBE` and its
pre-dispatch distance hook invokes the production managed enqueue path. Every
retained record has sixteen managed queue entries and zero individual renderer
calls. With skip disabled, the same resource is queued manually by sixteen
individual renderer calls. Both paths replay four cached pages.

Both arms keep pages enabled, unlimited cache/rebuild budget, and TTL100000.
Every aligned record requires queue16, flush2, pageCalls4, direct0, rebuild0,
cache4, M74 render/list0/0, immediate0, and eviction0.

Frozen trace:

```text
v1|design=2-balanced-pairs-skipTrue-false+false-true-same-plan-nonce|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|config=pagesTrue+min2+cacheNegative1+rebuildNegative1+ttl100000|skipTrue=managedQueue16+rendererCalls0|skipFalse=manualQueue16+rendererCalls16|both=flush2+pageCalls4+direct0+rebuild0+cached4+M74render0/list0+immediate0+evicted0|window=M74-min720intervals+12s|capture=unified-60byte-sidecar+same-index-census|stats=paired-descriptive-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `913fff54f216f47e06d3886f94f4682b83c1d5bbf49648991c28926d71e8c6f3`.

Timing values and directions remain descriptive and dynamic. M107 does not
claim generic block-entity compatibility, equivalent visual output, cost
attribution, causality, regression, improvement, inference, pixels, or
historical lag reproduction.
