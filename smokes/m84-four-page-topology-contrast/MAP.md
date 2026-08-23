<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ab9789101de12052aa945af741a37394c4a4b06fb78fa2d3d0737120a45eb39b -->

# M84 behavior map

Both fresh arms use the same sixteen-member scene and remove exactly three
members:

| Arm | Indices | Affected batch pages | Membership | Event rebuilds |
| --- | --- | --- | --- | --- |
| one | `0,1,2` | 1 | `16 -> 13` | 1 |
| three | `0,3,12` | 3 | `16 -> 13` | 3 |

Plan `(10,77,29)` straddles the Y=79/80 and Z=31/32 cell boundaries. Its
four natural page-key populations are `9/3/3/1`. The singleton is a direct
fallback under pinned Aero's `minInstances=2`; the other three pages are
cached batches. Both arms retain three cached pages/page calls, one direct
fallback, and two flush calls before and after the mutation.

The server validates topology, exact cells, block entities, and derived
nonces before mutation. The client requires ACK plus all three air blocks
before binding membership thirteen. The one-page arm leaves populations
`6/3/3/1`; the three-page arm leaves `8/2/2/1`.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-constant3member-one-vs-three-page-topology|fixture=constant16-synced+client-marker+exact-camera+y79-80+z31-32-boundaries+page-members9-3-3-1|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|warm-path=enqueue16+flush2+cached3+pageCalls3+direct1+rebuild0|events=request-after300+server-remove3-indices0-1-2-vs0-3-12+ack+client-blocks-air+membership13-both+rebuilds1-vs3+cache3+direct1|window=M74-min720intervals+12s|capture=M78-primitive-spans+page-counters+post-seal-topology-sidecar|per-record=M74-render1/list1+identity16to13+state16/maskffff|stats=descriptive-across-fresh-arms-dynamic|uninstrumented-cost-dose-response-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `ab9789101de12052aa945af741a37394c4a4b06fb78fa2d3d0737120a45eb39b`.

Nonclaims: additive per-page cost, performance direction, arbitrary topology,
arbitrary page population, additions, repeated/dense waves, persistence,
cleanup, causality, inference, pixels, cross-machine generality, combat, or
historical lag.
