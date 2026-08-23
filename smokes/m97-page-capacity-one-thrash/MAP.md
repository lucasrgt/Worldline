<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=93c51ccdd98d0abd4e6da174f6ea76d8ca10ddb31cfed965117945473a39c551 -->

# M97 behavior map

M97 reuses the exact M74/M78 sixteen-cell, four-page scene while limiting the
client page cache to one entry. TTL remains 100000 frames and the rebuild
budget remains eight, excluding expiry and budget fallback. Common/server code
is Aero-free; only the client presentation overlay supplies the Aero marker.

Every retained record must show:

- sixteen renderer calls/enqueues and two flush calls;
- four page calls, one cached page, and zero direct fallback;
- exactly four page rebuilds;
- a positive cumulative capacity-eviction count advancing by exactly four;
- M74 counters `0/0`, identity calls 16, state `0x1010`, mask `0xffff`.

With four requested page keys and one cache entry, no page from the prior frame
survives through the complete sorted flush. All four are compiled and each
compile displaces the previous cached page. The aligned 56-byte records bind
spans, page state, rebuilds, cache size, and cumulative evictions to the M74
nonce, plan, count, elapsed duration, and record index.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-four-page-capacity-one-thrash-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=enqueue16+flush2+maxCached1+cachedPages1+pageCalls4+direct0+rebuild4+evictedDelta4-per-record|window=M74-min720intervals+12s|capture=primitive-timers+page-counters+cumulative-evictions+post-seal-sidecar|per-record=M74-render0/list0/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `93c51ccdd98d0abd4e6da174f6ea76d8ca10ddb31cfed965117945473a39c551`.

Nonclaims: other capacities or topologies, TTL expiry, rebuild-budget fallback,
generic content, uninstrumented/additive cost, causality, regression or
improvement, inference, pixels, cross-machine generality, combat, or historical
lag reproduction.
