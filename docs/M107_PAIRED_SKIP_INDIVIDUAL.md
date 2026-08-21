# M107 paired skip-individual control

Status: GO in Worldline v1.95.0.

M107 compares literal `aero.becell.skipIndividual` values true and false over
the exact sixteen-identity M74 scene. Each pair reuses only its server-authored
plan and nonce; every arm has fresh server/client JVMs, world, worktrees, and
binary artifacts.

The true arm implements the pinned `Aero_CellPageRenderableBE` contract and
enters `tryQueueManagedAtRest` before individual renderer dispatch. It must
retain sixteen managed enqueues and zero renderer calls per interval. The false
arm keeps the same model resource but reaches public `queueAtRest` through
sixteen registered renderer calls. Both retain four cached pages, two flushes,
zero direct fallback/rebuild/eviction/immediate calls, and M74 render/list0/0.

The pre-dispatch hook credits an identity only after its exact coordinate and
server nonce reconcile with the official plan. The common/server closure stays
Aero-free. The runtime gate also proves skip literal, minimum2, pages enabled,
unlimited cache/rebuild sentinels, TTL100000, vanilla maximum frame limit, and
disabled Aero pacing.

M107 does not prove generic block-entity support, pixel or visual equivalence,
uninstrumented overhead, timing causality, performance improvement/regression,
statistical inference, or reproduction of historical lag.
