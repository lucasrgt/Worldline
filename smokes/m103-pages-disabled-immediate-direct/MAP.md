<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=7ebb83eada0eccda5dbb38d2610d92b60abe893d2483212710eb463e0aa285c6 -->

# M103 behavior map

M103 keeps the exact M74/M78 sixteen-entity scene but sets
`aero.becell.pages=false`. Cache maximum one, rebuild sentinel negative one,
and TTL100000 remain literal, although the disabled page gate makes them
inactive. Common/server code remains Aero-free.

Every retained record must show:

- sixteen renderer calls and sixteen `queueAtRest` entries;
- sixteen calls to the exact immediate model `drawDirect` overload;
- two empty flush calls;
- public queued/page/direct/rebuild/cache/eviction counters all zero;
- M74 renders/list calls `16/16`, identity calls16, state `0x1010`, mask
  `0xffff`.

The public `directFallbacksThisFrame` counter is zero at retained TAIL because
the later empty flush resets it. M103 therefore freezes a separate primitive
hook on the exact immediate overload and cross-checks it against M74 render
counters; it does not infer absence of work from the reset public state.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-pages-disabled-immediate-direct-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=queueEntry16+immediateDirect16+flush2+pagesDisabled+queued0+pageCalls0+publicDirect0+rebuild0+cached0+evicted0-per-record|window=M74-min720intervals+12s|capture=primitive-timers+immediate-direct-counter+page-counters+post-seal-sidecar|per-record=M74-render16/list16/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `7ebb83eada0eccda5dbb38d2610d92b60abe893d2483212710eb463e0aa285c6`.

Nonclaims: additive cost, relative performance, generic disabled-path
behavior, other render entrypoints or fixtures, causality,
regression/improvement, inference, pixels, cross-machine generality, combat,
or historical lag reproduction.
