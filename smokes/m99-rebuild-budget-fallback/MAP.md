# M99 behavior map

M99 keeps the exact M74/M78 sixteen-entity, four-page scene and the M97
one-entry cache, but lowers `aero.becell.rebuildsPerFrame` from eight to two.
TTL remains 100000. The client validates all three literal properties before
capture; common/server code remains Aero-free.

Every retained record must show:

- sixteen renderer calls/enqueues and two flush calls;
- two page calls, four direct-rendered instances, and two page rebuilds;
- `cachedPageCount=1` and a positive cumulative capacity-eviction count
  advancing by exactly two;
- M74 counters `4/4`, identity calls 16, state `0x1010`, mask `0xffff`.

For this fixed sorted page order, the two-page rebuild budget is consumed by
two page keys. The remaining two page keys contain four fixture instances in
total, which take the direct fallback. M99 freezes that exact bounded result;
it does not generalize the split to other membership layouts or page orders.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-four-page-capacity-one-rebuild-budget-two-fallback-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=enqueue16+flush2+maxCached1+cachedPages1+rebuildBudget2+pageCalls2+directInstances4+rebuild2+evictedDelta2-per-record|window=M74-min720intervals+12s|capture=primitive-timers+page-counters+cumulative-evictions+post-seal-sidecar|per-record=M74-render4/list4/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `bc072d0104007b86828550033fb0aa3e84c179aa5caee84dcd22552c3c9a4ce7`.

Nonclaims: other budgets, page-order independence, other membership layouts,
unlimited or zero cache, TTL expiry, generic content, uninstrumented/additive
cost, causality, regression/improvement, inference, pixels, cross-machine
generality, combat, or historical lag reproduction.
