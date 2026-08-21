# M101 rebuild-budget-zero direct path

Status: GO in Worldline v1.89.0.

M101 runs the exact four-page M74/M78 scene with a one-entry page cache, TTL
100000, and `aero.becell.rebuildsPerFrame=0`. Runtime validation proves the
literal properties before capture. Vanilla FPS remains unlimited and Aero
pacing remains disabled.

Every complete record must retain an empty cache while reporting sixteen
renderer and enqueue calls, two flush calls, zero page calls, zero rebuilds,
zero evictions, and sixteen direct instances. The aligned M74 census
independently reports sixteen renderer/list calls plus all sixteen synchronized
identities.

This closes the zero-budget endpoint: the rebuild gate prevents compilation
before any page can enter the cache, although the cache maximum itself remains
one. The result is bounded to this exact fixed scene and pinned Aero revision.

Nonclaims: other cache sizes or rebuild budgets, generic page-order behavior,
other membership layouts, TTL expiry, generic content, uninstrumented/additive
cost, causality, regression/improvement, inference, pixels, cross-machine
generality, combat, or historical lag reproduction.
