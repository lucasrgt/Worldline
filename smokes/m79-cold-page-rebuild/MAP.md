<!-- worldline-map-schema=1 -->
<!-- boundary=aero-cache-lifecycle -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=94b95453ff0ba5944e7592bbdd8251c064dd0d7aa966cfa2c8b343ce92267d08 -->

# M79 behavior map

M79 keeps the exact M78 four-page fixture warmed for 300 retained records,
then disposes the exact renderer model once at `GameRenderer` HEAD. No output
or file write occurs inside the retained bracket.

The single event must show:

- cached pages `4 -> 0 -> 4`;
- deleted-page and compiled-page cumulative deltas of exactly four;
- sixteen real enqueues, two flush calls, four page rebuilds/calls, and zero
  direct fallbacks;
- the same M74 state `0x1010`, mask `0xffff`, sixteen identities, and visible
  chunks as the warmed records.

Every non-event M78 record must keep rebuilds zero, cached pages/page calls
four, queued count sixteen, and calls `16/16/2`. A distinct fixed 68-byte
artifact identifies the event record and disposal span after seal.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-single-cold-rebuild-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|warm-path=enqueue16+flush2+cached4+pageCalls4+direct0+rebuild0|event=after300-records+dispose-cache4to0+deletedDelta4+rebuild4+compiledDelta4+cache4|window=M74-min720intervals+12s|capture=M78-primitive-spans+page-counters+post-seal-cold-sidecar|per-record=M74-render0/list0/identity16+state16/maskffff|stats=descriptive-event-vs-warm-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `94b95453ff0ba5944e7592bbdd8251c064dd0d7aa966cfa2c8b343ce92267d08`.

Nonclaims: generic/automatic invalidation, resource reload, uninstrumented or
additive cost, causal attribution, regression/improvement, inference, pixels,
cross-machine generality, combat relation, or historical lag reproduction.
