# M111 qualification cycle

`FixedSeedTerrainCycle` verifies the official dedicated-server byte length,
SHA-1 and SHA-256, compiles the protocol-14 adapter and smoke, then runs two
fresh server JVMs, client sessions and world directories.

Each client completes the initial play exchange, consumes Packet50/Packet51
until absolute chunk `(0,0)` is decoded, validates the full `16 x 128 x 16`
shape and nonempty columns, and closes cleanly. The runner requires both solid
occupancy rows, traces and signatures to match before checking frozen evidence.

Canonical evidence:

- servers: 2 fresh official JVMs;
- clients: 2 fresh protocol-14 sessions;
- solid blocks per chunk: 13,992;
- solid-occupancy SHA-256: `ffa29af83b49293c2b2a5a1791c55270bb11848d5b4f7532b77b5d45e506f946`;
- surface SHA-256: `bb5fa0b1c2f242c7952ec1e58d269d66705fa308fcdcc3b25a30c8b309ea74db`.

Diagnostic mode cannot qualify. The frozen semantic SHA-256 is
`1f477f68603f951d995e99d26d4dec79788d29a5ef1f29ecb30f5f490ccc0c2f`.
