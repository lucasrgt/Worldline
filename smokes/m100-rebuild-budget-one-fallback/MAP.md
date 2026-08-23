<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=322cccb6a7643bf79357d81d1c8b3ecf2bc0c7bcad170993ebbb01fc7fa8d76b -->

# M100 behavior map

M100 keeps the exact M74/M78 sixteen-entity scene, one-entry cache, and TTL
100000, but lowers `aero.becell.rebuildsPerFrame` to one. The client validates
all three literal properties before capture; common/server code remains
Aero-free.

Every retained record must have sixteen renderer/enqueue calls, two flush
calls, one rebuild, cache1, and cumulative capacity evictions advancing by
one. Two record modes must alternate strictly:

- `pageCalls=2`, `direct=4`, and aligned M74 renderer/list counters `4/4`;
- `pageCalls=1`, `direct=10`, and aligned M74 counters `10/10`.

Both modes must occur and their counts may differ by at most one. The pinned
loop alternates because a retained first page permits the second page to use
the sole rebuild, while the next frame's retained second page is evicted when
the first page consumes that rebuild. This explanation is bounded to the exact
page order and fixture.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-four-page-capacity-one-rebuild-budget-one-fallback-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=enqueue16+flush2+maxCached1+cachedPages1+rebuildBudget1+alternating(pageCalls2/direct4,pageCalls1/direct10)+rebuild1+evictedDelta1-per-record|window=M74-min720intervals+12s|capture=primitive-timers+page-counters+cumulative-evictions+post-seal-sidecar|per-record=M74-render4or10/listSame/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `322cccb6a7643bf79357d81d1c8b3ecf2bc0c7bcad170993ebbb01fc7fa8d76b`.

Nonclaims: other budgets, page-order independence, other membership layouts,
unlimited or zero cache, TTL expiry, generic content, uninstrumented/additive
cost, causality, regression/improvement, inference, pixels, cross-machine
generality, combat, or historical lag reproduction.
