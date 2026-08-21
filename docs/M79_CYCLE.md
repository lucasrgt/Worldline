# M79 qualification cycle

`ColdPageRebuildCycle` verifies the pinned Aero checkout and the Aero-free
server closure, builds Aero in a disposable worktree, and runs two fresh
graphical client/modded-server replicas. The second replica receives the first
replica's exact plan; nonce, camera, heap, page budget, frame limits, and census
window are equal.

M79 reuses M74 and M78 sources without modifying their frozen boundaries. A
client-only model accessor is invoked once after at least 300 retained records.
The hot window performs no event logging or artifact I/O. M74, M78, and M79
artifacts are written only after the complete census seals.

The cold parser requires its 68-byte schema and exact nonce/plan. It accepts a
dynamic cumulative baseline but requires cache `4/0/4`, compiled delta four,
deleted delta four, positive disposal duration, and an event index within the
census. The M78 parser requires exactly four rebuilds at that index and zero at
every other index, while preserving calls `16/16/2`, queued 16, page calls 4,
direct fallback 0, and cached pages 4 everywhere.

M74 still requires per-BE counters `0/0`, sixteen identity calls, state
`0x1010`, mask `0xffff`, and visible chunks at every record. Artifact marker
lengths and SHA-256 values, EOF, count, elapsed time, plan, and nonce are
cross-checked. Diagnostic mode cannot qualify.

The frozen semantic trace reproduces SHA-256
`94b95453ff0ba5944e7592bbdd8251c064dd0d7aa966cfa2c8b343ce92267d08`.
