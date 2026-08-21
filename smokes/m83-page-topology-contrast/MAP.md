# M83 behavior map

Both arms use the same sixteen-member scene and remove exactly two members:

| Arm | Indices | Affected pages | Membership | Event rebuilds |
| --- | --- | --- | --- | --- |
| same | `0,1` | 1 | `16 -> 14` | 1 |
| cross | `0,4` | 2 | `16 -> 14` | 2 |

Plan `(10,65,31)` straddles the Z=31/32 page boundary. The server validates
topology, exact cells, block entities, and nonces before mutation. The client
requires ACK plus both air blocks before binding the transition record.

Both arms retain two cached pages/page calls, two flush calls, and zero direct
fallback in every record. Rebuilds are zero outside the transition. A 44-byte
post-seal artifact binds topology and expected rebuild count to request/event
indices, plan, and nonce.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-constant2member-same-vs-cross-page-topology|fixture=constant16-synced+client-marker+exact-camera+z31-32-boundary|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|warm-path=enqueue16+flush2+cached2+pageCalls2+direct0+rebuild0|events=request-after300+server-remove2-indices0-1-vs0-4+ack+client-blocks-air+membership14-both+rebuilds1-vs2+cache2|window=M74-min720intervals+12s|capture=M78-primitive-spans+page-counters+post-seal-topology-sidecar|per-record=M74-render0/list0+identity16to14+state16/maskffff|stats=descriptive-across-fresh-arms-dynamic|uninstrumented-cost-dose-response-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `2418e988f23571a72a07c2521eb9ee7cb9ebc8b436957a74d7cf226fe4878f10`.

Nonclaims: additive page cost, performance direction, arbitrary topology,
additions, repeated/dense waves, persistence, cleanup, causality, inference,
pixels, cross-machine generality, combat, or historical lag.
