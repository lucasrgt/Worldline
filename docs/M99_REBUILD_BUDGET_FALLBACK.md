# M99 rebuild-budget fallback

Status: GO in Worldline v1.87.0.

M99 runs the exact four-page M74/M78 scene with a one-entry page cache, TTL
100000, and `aero.becell.rebuildsPerFrame=2`. Runtime validation proves the
literal properties before capture. Vanilla FPS remains unlimited and Aero
pacing remains disabled.

Every complete record must retain cache1 while reporting sixteen renderer and
enqueue calls, two flush calls, two page calls, two rebuilds, and four direct
fallback instances. The cumulative capacity-eviction counter must advance by
two in the same record. The aligned M74 census independently reports four
direct renderer/list calls plus all sixteen synchronized identities.

This is an exact fixed-order boundary: the two-page budget compiles two sorted
page keys, while the remaining two keys contain four instances that render
directly. It does not establish the same instance split for another topology,
membership distribution, or page iteration order.

Nonclaims: other rebuild budgets, page-order independence, other membership
layouts, unlimited or zero cache, TTL expiry, generic content,
uninstrumented/additive cost, causality, regression/improvement, inference,
pixels, cross-machine generality, combat, or historical lag reproduction.
