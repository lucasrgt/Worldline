# M104 behavior map

M104 compares the same exact M74/M78 sixteen-entity scene with cell pages
enabled and disabled. Two fresh pairs run in balanced order: enabled/disabled,
then disabled/enabled. Each pair shares its server-authored plan, camera,
registry identity, and nonce; JVMs, worlds, clients, and servers remain fresh.

Both arms retain sixteen renderer and `queueAtRest` entries plus two flush
calls in every aligned record. The enabled arm must show queued16, pageCalls4,
rebuild4, cache1, eviction increasing by four, no immediate calls, and M74
render/list counters0/0. The disabled arm must show immediateDirect16, all
public page counters zero, and M74 render/list counters16/16.

The 60-byte sidecar is identical in both arms and is index-aligned with the
M74 census. Time summaries and paired differences are descriptive, dynamic,
and excluded from release qualification.

Frozen trace:

```text
v1|design=2-balanced-pairs-enabled-disabled+disabled-enabled-same-plan-nonce|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|enabled=pagesTrue+queueEntry16+immediate0+flush2+queued16+pageCalls4+rebuild4+cache1+evictedDelta4+M74render0/list0|disabled=pagesFalse+queueEntry16+immediate16+flush2+queued0+pageCalls0+rebuild0+cache0+evicted0+M74render16/list16|window=M74-min720intervals+12s|capture=unified-60byte-sidecar+same-index-census|stats=paired-descriptive-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `a91f910fbbf2ced951e0a009e1db64924f8b8a33f34aeca4f8b0e6b6e2bc4df8`.

Nonclaims: uninstrumented or additive cost, causality, regression or
improvement, inference, significance, pixels, cross-machine generality,
combat, or reproduction/attribution of historical lag.
