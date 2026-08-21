# M80 natural membership rebuild

Status: GO in Worldline v1.68.0.

M80 replaces M79's explicit client cache disposal with one server-authored
content membership change. After record 300, the client sends a typed
StationAPI request identifying the synchronized plan origin and root nonce.
The Aero-free server closure validates exact cell zero, removes it with the
normal world mutation, and returns an exact acknowledgement.

The client requires the acknowledged coordinate to contain air and the real
renderer path to change from sixteen identities to fifteen. The first altered
record contains exactly one page rebuild, two cached pages/page calls, fifteen
renderer/enqueue calls, two flush calls, and zero direct fallback. All earlier
records contain sixteen identities; all later records contain fifteen, with no
further rebuild.

M74's state and mask remain `0x1010`/`0xffff` because they are historical proof
that all sixteen synchronized identities were received before acquisition.
The per-record renderer call count is the live membership oracle. A legacy
client block-entity lookup may remain stale after the block becomes air, so BE
lookup cleanup is explicitly outside the milestone.

Two fresh same-plan/nonce replicas requested at record 300 and observed the
first fifteen-member records at 409 and 450. Their event renderer/enqueue/flush
spans were respectively `3600/1900/85000 ns` and `3700/2100/86300 ns`.
Those values are descriptive under nested instrumentation, not thresholds or
isolated costs.

Nonclaims: generic invalidation, additions, dense rebuild waves, BE cleanup,
persistence, uninstrumented or additive cost, causality, regression or
improvement, inference, pixels, cross-machine generality, combat relation, or
historical lag reproduction.
