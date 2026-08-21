# M108 paired cell size

Status: GO in Worldline v1.96.0.

M108 compares literal `aero.becell.size` values two and eight over the exact
sixteen-identity M74 scene. Each pair reuses only its fixed aligned plan and
nonce; every arm has fresh server/client JVMs, world, worktrees, and binary
artifacts.

The plan `(2,80,0)` spans two cells on Y and two on Z at size two, producing
four cached page calls with four members each. At size eight the same plan fits
one cell and produces one cached page call with all sixteen members. Both arms
retain sixteen queue entries and individual renderer calls, two flushes, zero
direct fallback/rebuild/eviction/immediate calls, and M74 render/list `0/0`.

The runtime gate proves the public cell-size literal, minimum2,
skip-individual false, pages enabled, unlimited cache/rebuild sentinels,
TTL100000, vanilla maximum frame limit, and disabled Aero pacing. The
common/server closure remains Aero-free.

M108 does not prove generic cell-size policy, memory cost, visual equivalence,
uninstrumented overhead, timing causality, a better size, performance
improvement/regression, statistical inference, or historical lag reproduction.
