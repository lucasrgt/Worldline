# M121 qualification cycle

`FixedSeedRegionCycle` verifies the official server artifact, compiles the
published API, adapter and smoke, and constructs two fresh world replicas.
Each replica uses one official server/client pair to load and settle the 3x3
region, then a second official server/client pair after clean save and restart
to obtain the authoritative Packet51 census.

The two replicas must match block count, column count, aggregate solid count,
exact surface SHA-256, internal solid-seam SHA-256, semantic trace and
signature. Interior position-mask, raw ID and metadata hashes remain recorded
separately so runtime nondeterminism is visible without weakening the frozen
regional surface claim.
Pending or diagnostic descriptors cannot qualify.

Canonical evidence uses four official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`c2a08f5e7e5ec2b6767afbc4b26409d586f2fd4ca296d199d823abe8b2b73d4f`.
