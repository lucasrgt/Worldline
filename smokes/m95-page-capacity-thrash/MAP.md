# M95 behavior map

M95 reuses the frozen M74 server-authored sixteen-cell scene and M78's exact
four-page topology, but bounds Aero's client page cache to three entries.
The page TTL is pinned to 100000 frames so this boundary isolates capacity
eviction from M94's expiry path. A client-only Mixin adds Aero's
`Aero_CellRenderableBE` marker; no Aero type enters the common/server closure.

For the fixed camera and 4x4 Y/Z wall, every retained record must show:

- sixteen Worldline renderer calls and sixteen real `queueAtRest` enqueues;
- two `flush` calls, one empty failsafe and one populated render flush;
- `queuedLastFrame=16`, `cachedPageCount=3`, and `pageCallsThisFrame=4`;
- `directFallbacks=0` and exactly three page rebuilds;
- a positive cumulative eviction count that advances by exactly three from
  every record to the next;
- M74 per-BE counters `0/0`, identity calls 16, state `0x1010`, mask `0xffff`.

The four requested pages cannot coexist in the three-entry cache. Under the
pinned replacement policy, one page survives into the next frame and three are
rebuilt and evicted on every retained record. The aligned 56-byte-record
sidecar carries the direct renderer/enqueue/flush spans, page state, rebuilds,
cache size, and cumulative capacity evictions. It shares nonce, plan, record
count, elapsed duration, and record index with the M74 census and is written
only after seal.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-four-page-capacity-three-thrash-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=enqueue16+flush2+maxCached3+cachedPages3+pageCalls4+direct0+rebuild3+evictedDelta3-per-record|window=M74-min720intervals+12s|capture=primitive-timers+page-counters+cumulative-evictions+post-seal-sidecar|per-record=M74-render0/list0/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `1299a1e62338199b84fe116547d929116bdc23e793cf28a6e466f77e15bd3bed`.

Nonclaims: other cache sizes or replacement policies, TTL expiry, direct
fallback, generic content, uninstrumented or additive cost, causality,
regression/improvement, inference, pixels, cross-machine generality, combat
relation, or historical lag reproduction.
