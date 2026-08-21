# M109 cell-size floor

Status: GO in Worldline v1.97.0.

M109 compares raw `aero.becell.size` values zero and one over the same fixed
sixteen-identity M74 scene. Each pair reuses only plan and nonce; every arm has
fresh server/client JVMs, world, worktrees, and binary artifacts.

The pinned `Aero_BECellIndex` clamps raw zero to its supported lower bound one.
At effective size one every identity occupies a distinct cell. Minimum2 means
both arms retain sixteen queue and individual renderer calls, zero page calls,
sixteen direct fallbacks, an empty cache, two flushes, and M74 render/list
`16/16`, with no rebuild, eviction, or immediate call.

The runtime gate proves each raw literal separately and the common public
effective value one. It also freezes skip-individual false, pages enabled,
unlimited cache/rebuild sentinels, TTL100000, maximum frame limit, disabled
Aero pacing, exact camera, and Aero-free common/server closure.

M109 does not prove generic clamping, configuration quality, memory cost,
visual equivalence, uninstrumented overhead, timing causality, performance
improvement/regression, statistical inference, or historical lag reproduction.
