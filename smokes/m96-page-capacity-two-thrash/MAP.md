# M96 behavior map

M96 reuses the frozen M74 server-authored sixteen-cell scene and M78's exact
four-page topology, but bounds Aero's client page cache to two entries. Page
TTL remains 100000 frames, separating capacity eviction from M94's expiry
path. A client-only Mixin supplies the Aero marker; common/server code remains
Aero-free.

Every retained record must show:

- sixteen renderer calls, sixteen real enqueues, and two flush calls;
- `queuedLastFrame=16`, `cachedPageCount=2`, and four page calls;
- zero direct fallback and either three or four page rebuilds;
- a positive cumulative capacity-eviction count whose per-record delta equals
  that record's rebuild count exactly;
- M74 counters `0/0`, identity calls 16, state `0x1010`, mask `0xffff`.

The pinned replacement path has bounded JVM-dependent tie behavior when cached
pages share `lastUsedFrame`: observed replicas can retain one prior key and
rebuild three, or rebuild all four. The semantic oracle accepts only those two
modes and requires eviction/rebuild coupling on every record; it does not
require a particular mode assignment. The aligned 56-byte-record sidecar binds
spans, page state, rebuilds, cache size, and cumulative evictions to the M74
nonce, plan, record count, elapsed duration, and record index.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-four-page-capacity-two-bounded-thrash-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=enqueue16+flush2+maxCached2+cachedPages2+pageCalls4+direct0+rebuildIn3or4+evictedDeltaEqualsRebuilds-per-record|window=M74-min720intervals+12s|capture=primitive-timers+page-counters+cumulative-evictions+post-seal-sidecar|per-record=M74-render0/list0/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `96142417765b773152dc82aba8194765319c2c7bd987d513c5b8b8fd34b89acb`.

Nonclaims: other cache sizes, a general capacity/rebuild law, TTL expiry,
generic content, uninstrumented or additive cost, causality, regression or
improvement, inference, pixels, cross-machine generality, combat, or historical
lag reproduction.
