# M141 Pig spawner observation

Status: GO in Worldline v1.129.0.

M141 opens Worldline's official living-entity layer. It adds immutable
`RemoteMobSpawn` evidence and a cumulative `MobObservationSession` API. The
server adapter decodes bounded Packet24 identity, type, fixed-point pose,
rotation and the complete protocol-14 metadata type vocabulary while rejecting
duplicate indices, missing base flags and queue overflow.

The official smoke builds a raised grass platform and places default mob
spawner `52:0`. The server creates a pig and two simultaneous clients receive
the identical type-`90` Packet24 with one shared positive non-player entity ID,
two metadata entries, flags zero and a pose inside the spawner's official
random volume. A production-path byte fixture independently freezes the packet
field widths and metadata terminator.

M141 does not claim natural spawning, arbitrary mob types or metadata meaning,
movement, pathfinding, AI decisions, combat, drops, breeding, saddle state,
entity persistence across restart, despawn, Packet24 timing or exact randomized
spawn coordinates.
