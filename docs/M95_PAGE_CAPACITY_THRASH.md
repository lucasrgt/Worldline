# M95 page-capacity thrash

Status: GO in Worldline v1.83.0.

M95 retains M78's exact four-page, sixteen-cell scene while setting Aero's
maximum cached pages to three. The page TTL is fixed at 100000 frames so no
TTL sweep participates in the retained window. Runtime validation also fixes
the rebuild budget at eight and disables both the vanilla frame limit and the
Aero frame pacer.

Every complete retained record in both fresh replicas had sixteen renderer
calls, sixteen real enqueues, two flush calls, four page calls, three cached
pages and zero direct fallback. Each record rebuilds between one and four pages,
and its cumulative capacity-eviction delta must equal that rebuild count. The
evidence summary reports the observed count for every rebuild mode. No flush
span may be negative, and the aggregate populated-flush time stays positive.

This qualifies a pinned replacement-policy thrash path: four requested page
keys cannot coexist in a three-entry cache. Equal `lastUsedFrame` ties follow
the pinned `HashMap` iteration order, so the set of upcoming keys that survives
is not fixed at two. Values, rebuild-mode counts, and span summaries are
descriptive and specific to this exact revision, scene, camera, and cache size.

Nonclaims: other capacities or replacement policies, TTL expiry, generic
content, uninstrumented or additive cost, causal attribution, regression or
improvement, inference, pixels, cross-machine generality, combat, or historical
lag reproduction.
