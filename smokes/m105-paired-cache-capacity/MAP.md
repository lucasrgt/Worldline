# M105 behavior map

M105 compares literal cache capacity one with literal negative-one unlimited
capacity while keeping pages enabled, rebuild sentinel negative one, TTL100000,
and the exact M74/M78 sixteen-entity scene. Two fresh pairs run in balanced
order and share only their server-authored plan and nonce inside each pair.

Before teleport, the server places and verifies one vanilla stone support
block outside the sixteen Aero cells so gravity cannot change the paired pose.

Every aligned record has sixteen renderer/queue entries, two flushes, four
page calls, no immediate calls, and M74 render/list counters0/0. Capacity one
must rebuild four pages, retain one, and advance eviction by four per record.
Unlimited capacity must retain four pages with rebuild0 and eviction0.

The same 60-byte sidecar and parser cover both arms. Per-arm timing summaries
are descriptive and dynamic; no timing direction participates in GO.

Frozen trace:

```text
v1|design=2-balanced-pairs-cache1-unlimited+unlimited-cache1-same-plan-nonce|fixture=constant16-synced+client-marker+exact-camera|server-closure=aero-free|frame-limit=vanilla-max0+aero-pacer-off-runtime-gated|config=pagesTrue+rebuildNegative1+ttl100000|cache1=queueEntry16+immediate0+flush2+queued16+pageCalls4+rebuild4+cached1+evictedDelta4|unlimited=queueEntry16+immediate0+flush2+queued16+pageCalls4+rebuild0+cached4+evicted0|both=M74render0/list0|window=M74-min720intervals+12s|capture=unified-60byte-sidecar+same-index-census|stats=paired-descriptive-dynamic|uninstrumented-cost-causality-regression-historical-lag=not-claimed|shutdown=clean
```

SHA-256: `35da2fabb47ef902a2cbd7b92dc976771d9a80179b76322cf1f26edade4e5898`.

Nonclaims: generic cache behavior, memory cost, uninstrumented/additive cost,
causality, regression/improvement, inference, significance, pixels,
cross-machine generality, combat, or historical lag attribution.
