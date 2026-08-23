<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f30116757d3fcf070289bdb013181744abdaf8da806426cc2efc76128484bc6d -->

# M81 behavior map

M81 keeps a synchronized sixteen-identity scene warm on two natural Aero page
keys, then asks the server to remove exact plan indices zero and eight after
retained record 300. The fixed plan `(10,65,31)` straddles Z=31/32, so the two
targets belong to distinct pages. The request contains only the plan origin
and root nonce.

The first retained record with fourteen renderer identities must show:

- the exact acknowledgement and air at both requested client coordinates;
- calls/enqueues/queued membership `16 -> 14` with zero direct fallback;
- exactly two page rebuilds, two page calls, and two cached pages;
- unchanged historical state `0x1010` and mask `0xffff`.

All earlier records contain sixteen identities and all later records fourteen.
Rebuilds are zero outside the transition. A fixed 40-byte post-seal artifact
binds request/transition indices, both targets, nonce, and plan.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-natural-two-page-rebuild-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|warm-path=enqueue16+flush2+cached2+pageCalls2+direct0+rebuild0|event=request-after300+server-remove-indices0and8+ack+two-client-blocks-air+membership16to14+rebuild2+cache2|window=M74-min720intervals+12s|capture=M78-primitive-spans+page-counters+post-seal-wave-sidecar|per-record=M74-render0/list0+identity16to14+state16/maskffff|stats=descriptive-event-vs-prechange-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `f30116757d3fcf070289bdb013181744abdaf8da806426cc2efc76128484bc6d`.

Nonclaims: arbitrary topology, additions, repeated/dense waves, block-entity
cleanup, persistence, uninstrumented/additive cost, causal attribution,
regression/improvement, inference, pixels, cross-machine generality, combat
relation, or historical lag reproduction.
