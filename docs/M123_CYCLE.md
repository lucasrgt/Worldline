# M123 qualification cycle

`CrossChunkLightingCycle` verifies the official server artifact, compiles the
published API, adapter and smoke, and repeats the source intervention in two
fresh worlds. Each world uses an actor for Packet15/Packet53 and a fresh reader
for authoritative Packet51 snapshots of both adjacent chunks.

Both runs must reproduce source/neighbor coordinates and states, exact sample
values, four plane deltas, ordered delta hashes, semantic trace and signature.
Pending or diagnostic descriptors cannot qualify. Canonical evidence uses two
official server JVMs and four client sessions.

The frozen semantic SHA-256 is
`7f93c32c82a360dcdc5c546f69838e8fcbc8a221bf8ad2961bd532876608365a`.
