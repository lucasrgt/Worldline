# M102 behavior map

M102 keeps the exact M74/M78 sixteen-entity, four-page scene, one-entry cache,
and TTL 100000, but passes literal
`aero.becell.rebuildsPerFrame=-1`. The pinned Aero predicate accepts every
rebuild when the configured value is negative. The client validates all three
properties before capture; common/server code remains Aero-free.

Every retained record must show:

- sixteen renderer calls/enqueues and two flush calls;
- four page calls, zero direct-rendered instances, and four rebuilds;
- `cachedPageCount=1` and cumulative capacity evictions advancing by four;
- M74 counters `0/0`, identity calls 16, state `0x1010`, mask `0xffff`.

The negative-one sentinel removes the per-frame rebuild ceiling. With four
requested page keys and one cache entry, all four compile and each displaces
the previous cached page. This exact observed path matches the unbounded gate;
M102 does not claim equivalence for arbitrary negative values or fixtures.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-four-page-capacity-one-negative-unlimited-rebuild-sentinel-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=enqueue16+flush2+maxCached1+cachedPages1+rebuildBudgetNegativeOneUnlimited+pageCalls4+direct0+rebuild4+evictedDelta4-per-record|window=M74-min720intervals+12s|capture=primitive-timers+page-counters+cumulative-evictions+post-seal-sidecar|per-record=M74-render0/list0/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `852d41f2d1654fd1dc83d0b746fddb4c109d370573fd67b25290361ddaefa75b`.

Nonclaims: other negative values or capacities, TTL expiry, other topologies,
generic content, uninstrumented/additive cost, causality,
regression/improvement, inference, pixels, cross-machine generality, combat,
or historical lag reproduction.
