# M131 dual-dimension session

Status: GO in Worldline v1.119.0.

M131 adds `DimensionSession` above the existing cumulative multiplayer API.
`B173WireClient` retains the signed Packet1 dimension and exposes bounded
waiting for an exact vanilla dimension.

The official smoke keeps Overworld and Nether players connected concurrently,
decodes distinct world evidence and persists dimensions `0` and `-1`.
Packet9 dimension changes invalidate all cached chunks; redundant same-dimension
respawns preserve them.

M131 does not claim portal traversal, actual live Packet9 from the server,
respawn pose, entity isolation across dimensions, Overworld return, or arbitrary
dimension IDs.
