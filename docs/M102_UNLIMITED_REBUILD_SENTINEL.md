# M102 unlimited-rebuild sentinel

Status: GO in Worldline v1.90.0.

M102 runs the exact four-page M74/M78 scene with a one-entry page cache, TTL
100000, and literal `aero.becell.rebuildsPerFrame=-1`. The pinned Aero gate
treats any negative value as unlimited. Runtime validation proves the literal
properties before capture; common/server code remains Aero-free.

Every complete record must report sixteen renderer/enqueue calls, two flush
calls, four page calls, four rebuilds, one cached page, zero direct instances,
and cumulative capacity evictions advancing by four. The aligned M74 census
independently reports zero direct renderer/list calls and all sixteen
synchronized identities.

This qualifies only the exact negative-one sentinel and fixed fixture. It does
not generalize arbitrary negative values, cache sizes, membership layouts, or
page order.

Nonclaims: other negative values or capacities, TTL expiry, other topologies,
generic content, uninstrumented/additive cost, causality,
regression/improvement, inference, pixels, cross-machine generality, combat,
or historical lag reproduction.
