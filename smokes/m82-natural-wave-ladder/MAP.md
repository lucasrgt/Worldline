<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=2727138a7c9b2eb9e38b7a40a9ae8518a3c3c7b0739c188d2ae152edbbb47bab -->

# M82 behavior map

M82 uses three fresh arms with the same exact two-page scene:

| Targets | Exact indices | Membership | Event rebuilds | Cached pages |
| --- | --- | --- | --- | --- |
| 1 | `0` | `16 -> 15` | 1 | 2 |
| 2 | `0,4` | `16 -> 14` | 2 | 2 |
| 4 | `0,1,4,5` | `16 -> 12` | 2 | 2 |

The fixed plan `(10,65,31)` crosses Z=31/32. Indices zero/one are on the first
page; four/five are on the second. Each arm sends one typed request after
record 300. The server validates every exact cell and nonce, removes all
targets, and acknowledges; the client requires all targets to be air before
binding the first reduced-membership record.

Every record retains two flush calls, two cached pages/page calls, and zero
direct fallback. Rebuilds are zero outside the single event record. A 44-byte
post-seal artifact binds cardinality and rebuild expectation to plan, nonce,
and request/event indices.

Frozen trace:

```text
v1|design=3-fresh-same-plan-nonce-natural-membership-wave-cardinalities1-2-4|fixture=constant16-synced+client-marker+exact-camera+z31-32-boundary|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|warm-path=enqueue16+flush2+cached2+pageCalls2+direct0+rebuild0|events=request-after300+server-remove-balanced-targets1-2-4+ack+client-blocks-air+membership15-14-12+rebuilds1-2-2+cache2|window=M74-min720intervals+12s|capture=M78-primitive-spans+page-counters+post-seal-cardinality-sidecar|per-record=M74-render0/list0+identity16to15-14-12+state16/maskffff|stats=descriptive-across-fresh-arms-dynamic|uninstrumented-cost-dose-response-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `2727138a7c9b2eb9e38b7a40a9ae8518a3c3c7b0739c188d2ae152edbbb47bab`.

Nonclaims: performance/dose response, additive/member/page cost, arbitrary
topology/cardinality, additions, repeated waves, persistence, cleanup,
causality, regression/improvement, inference, pixels, or historical lag.
