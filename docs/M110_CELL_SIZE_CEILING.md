# M110 cell-size ceiling

Status: GO in Worldline v1.98.0.

M110 compares raw `aero.becell.size` values thirty-three and thirty-two over
the same fixed sixteen-identity M74 scene. Each pair reuses only plan and
nonce; every arm has fresh server/client JVMs, world, worktrees, and artifacts.

The pinned `Aero_BECellIndex` clamps raw thirty-three to its supported upper
bound thirty-two. At effective size thirty-two the complete wall occupies one
cell. Both arms retain sixteen queue and renderer calls, one cached page call,
two flushes, M74 render/list `0/0`, and zero direct fallback, rebuild,
eviction, or immediate call.

The runtime gate proves each raw literal separately and the common public
effective value thirty-two. It also freezes minimum2, skip-individual false,
pages enabled, unlimited cache/rebuild, TTL100000, maximum frame limit,
disabled Aero pacing, exact camera, and Aero-free common/server closure.

M110 does not prove generic clamping, configuration quality, memory cost,
visual equivalence, uninstrumented overhead, timing causality, performance
improvement/regression, statistical inference, or historical lag reproduction.
