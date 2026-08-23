<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8e0d8ae9c249c8f2967e0ac534c0ee7b7e79ff6a04bd7b407c89dcd2f5e7b0cd -->

# M101 behavior map

M101 keeps the exact M74/M78 sixteen-entity, four-page scene, one-entry cache,
and TTL 100000, but sets `aero.becell.rebuildsPerFrame=0`. The client validates
all three literal properties before capture; common/server code remains
Aero-free.

Every retained record must show:

- sixteen renderer calls/enqueues and two flush calls;
- zero page calls, sixteen direct-rendered instances, and zero rebuilds;
- `cachedPageCount=0`, zero capacity evictions, and no cached page keys;
- M74 counters `16/16`, identity calls 16, state `0x1010`, mask `0xffff`.

The zero rebuild budget prevents page compilation before the one-entry cache
can be populated. Consequently all sixteen synchronized fixture instances use
the direct path. The cache maximum remains one and is proved as an unused
ceiling; M101 does not disable paging through the cache-size property.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-four-page-capacity-one-rebuild-budget-zero-direct-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=enqueue16+flush2+maxCached1+cachedPages0+rebuildBudget0+pageCalls0+directInstances16+rebuild0+evicted0-per-record|window=M74-min720intervals+12s|capture=primitive-timers+page-counters+cumulative-evictions+post-seal-sidecar|per-record=M74-render16/list16/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `8e0d8ae9c249c8f2967e0ac534c0ee7b7e79ff6a04bd7b407c89dcd2f5e7b0cd`.

Nonclaims: other cache sizes or budgets, generic page-order behavior, other
membership layouts, TTL expiry, generic content, uninstrumented/additive cost,
causality, regression/improvement, inference, pixels, cross-machine
generality, combat, or historical lag reproduction.
