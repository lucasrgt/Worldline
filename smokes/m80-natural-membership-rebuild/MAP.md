<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=3df82b51703daacc031e1f745f86fc7af6678d2da74901eb6c00183915e8a77a -->

# M80 behavior map

M80 keeps one synchronized sixteen-identity Aero scene warmed, then asks the
server to remove exact cell index zero after retained record 300. The request
contains only the frozen plan origin and root nonce. The server validates the
block, block entity, and derived cell nonce before replacing that block with
air and returning an exact acknowledgement.

The first retained record with fifteen renderer identities must show:

- the acknowledgement and an air block at the requested client coordinate;
- calls/enqueues/queued membership `16 -> 15` with no direct fallback;
- exactly one page rebuild, two page calls, and two cached pages;
- the unchanged historical M74 synchronized state `0x1010` and mask `0xffff`.

Every earlier record must contain sixteen identities and every later record
fifteen. Rebuilds are zero outside the transition record. A fixed 36-byte
post-seal artifact binds request and transition indices to nonce and plan.

The legacy client can retain a stale block-entity lookup after the block is
air. M80 does not claim block-entity lookup cleanup; the qualified boundary is
the server-authored block removal and renderer-membership transition.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-natural-membership-rebuild-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|warm-path=enqueue16+flush2+cached2+pageCalls2+direct0+rebuild0|event=request-after300+server-remove-index0+ack+client-block-air+membership16to15+rebuild1+cache2|window=M74-min720intervals+12s|capture=M78-primitive-spans+page-counters+post-seal-membership-sidecar|per-record=M74-render0/list0+identity16to15+state16/maskffff|stats=descriptive-event-vs-prechange-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `3df82b51703daacc031e1f745f86fc7af6678d2da74901eb6c00183915e8a77a`.

Nonclaims: generic membership invalidation, block-entity cleanup, additions or
multi-cell waves, persistence, uninstrumented/additive cost, causal
attribution, regression/improvement, inference, pixels, cross-machine
generality, combat relation, or historical lag reproduction.
