<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=0da3de05b8d5c493b974e04eaf1767e07f54b087f387badfdaf5dd48b6f1bb31 -->

# M98 behavior map

M98 configures `aero.becell.maxCachedPages=0` on the exact M74/M78 four-page
scene. The client runtime validates that literal zero before capture. TTL stays
100000 and rebuild budget eight; common/server code remains Aero-free.

Despite configured zero, every retained record must show:

- sixteen renderer calls/enqueues and two flush calls;
- four page calls, zero direct fallback, and four rebuilds;
- `cachedPageCount=1`, not zero;
- a positive cumulative capacity-eviction count advancing by four;
- M74 counters `0/0`, identity calls 16, state `0x1010`, mask `0xffff`.

The floor follows the pinned eviction implementation. After compiling a page,
`enforceMaxCachedPages` excludes the new protected key from victim selection.
When it is the only entry and the configured maximum is zero, no victim exists,
so that protected entry remains. Each next compile evicts the prior entry and
again retains the protected one. M98 qualifies this exact behavior; it does not
interpret zero as disabling the paging feature.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-four-page-configured-zero-protected-floor-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=enqueue16+flush2+configuredMax0+protectedCachedPages1+pageCalls4+direct0+rebuild4+evictedDelta4-per-record|window=M74-min720intervals+12s|capture=primitive-timers+page-counters+cumulative-evictions+post-seal-sidecar|per-record=M74-render0/list0/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `0da3de05b8d5c493b974e04eaf1767e07f54b087f387badfdaf5dd48b6f1bb31`.

Nonclaims: paging disabled, other negative/positive configurations, alternative
replacement policies, TTL expiry, generic content, uninstrumented/additive
cost, causality, regression/improvement, inference, pixels, cross-machine
generality, combat, or historical lag reproduction.
