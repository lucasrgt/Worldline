# M82 natural wave ladder

Status: GO in Worldline v1.70.0.

M82 holds the M81 two-page scene, seed, plan, nonce, camera, page budget, and
complete-census window constant while varying one exact server-authored
removal wave across three fresh arms.

The one-target arm removes index zero from the Z=31 page. The two-target arm
removes indices zero and four, one on each page. The four-target arm removes
indices zero, one, four, and five, two on each page. Every request is validated
against block, block-entity, coordinate, root nonce, derived per-cell nonce,
and configured cardinality before the Aero-free server mutates the world and
returns an exact acknowledgement.

The client requires every requested block to be air. Membership becomes 15,
14, and 12 respectively. The first reduced-membership record contains one,
two, and two page rebuilds respectively; both natural pages remain cached and
called, flush calls remain two, and direct fallback stays zero. This establishes
that the qualified rebuild count follows affected pages rather than removed
member count for these exact arms.

The M82 overlay normalizes yaw/pitch to the already frozen `-90/0` camera
before the inherited strict M74 readiness check; position, height, chunks, and
empty plan cells remain independently validated.

The qualified arms observed their first reduced-membership records at 312,
375, and 332. Their event renderer/enqueue/flush spans were respectively
`4800/3400/94700 ns`, `3700/2400/89300 ns`, and
`5900/4000/120500 ns`. These are descriptive instrumented observations, not a
cardinality trend, threshold, or isolated cost.

Nonclaims: a performance or dose response, additive/member/page cost,
arbitrary cardinality or topology, additions, repeated waves, block-entity
cleanup, persistence, causality, regression/improvement, inference, pixels,
cross-machine generality, combat relation, or historical lag reproduction.
