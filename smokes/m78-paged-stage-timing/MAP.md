# M78 behavior map

M78 reuses the frozen M74 server-authored sixteen-cell scene. A client-only
Mixin adds Aero's `Aero_CellRenderableBE` marker to the remote presentation
class; no Aero type enters the common/server class closure.

For the exact fixed camera and 4x4 Y/Z wall, every retained record must show:

- sixteen Worldline renderer calls and sixteen real `queueAtRest` enqueues;
- two `flush` calls, one empty failsafe and one populated render flush;
- `queuedLastFrame=16`, `cachedPageCount=4`, and `pageCallsThisFrame=4`;
- `directFallbacks=0` and `pageRebuilds=0`;
- M74 per-BE counters `0/0`, identity calls 16, state `0x1010`, mask `0xffff`.

The page topology and lack of rebuilds describe a warmed steady-state bracket.
The aligned sidecar carries direct renderer/enqueue/flush spans and the five
page counters. It shares nonce, plan, count, elapsed duration, and record index
with the M74 census and is written only after seal.

Frozen trace:

```text
v1|design=2-fresh-same-plan-nonce-paged-replicas|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|path=enqueue16+flush2+cachedPages4+pageCalls4+direct0+rebuild0|window=M74-min720intervals+12s|capture=primitive-timers+page-counters+post-seal-sidecar|per-record=M74-render0/list0/identity16+state16/maskffff|stats=descriptive-paged-spans-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `dbb52fb098cf377aa90027c4000ab7073efa6cbe5bc4f4fa56fa2090d38ae894`.

Nonclaims: cold page compilation/rebuilds, generic content, uninstrumented or
additive cost, causality, regression/improvement, inference, pixels,
cross-machine generality, combat relation, or historical lag reproduction.
