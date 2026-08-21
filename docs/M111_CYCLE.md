# M111 qualification cycle

`FixedSeedTerrainCycle` verifies the official dedicated-server byte length,
SHA-1 and SHA-256, compiles the protocol-14 adapter and smoke, then runs two
fresh server JVMs, client sessions and world directories.

Each client completes the initial play exchange, consumes Packet50/Packet51
until absolute chunk `(0,0)` is decoded, validates the full `16 x 128 x 16`
shape and nonempty columns, and closes cleanly. The runner requires both
terrain rows, traces and signatures to match before checking frozen evidence.

Canonical evidence:

- servers: 2 fresh official JVMs;
- clients: 2 fresh protocol-14 sessions;
- non-air blocks per chunk: 16,342;
- full block-ID SHA-256: `a51059341212e1be0b41cea24881a07e962ffa39a4aa4dc874a76fc61e4326bf`;
- surface SHA-256: `bb5fa0b1c2f242c7952ec1e58d269d66705fa308fcdcc3b25a30c8b309ea74db`.

Diagnostic mode cannot qualify. The frozen semantic SHA-256 is
`1242a03c15a6e0c36adbefb6ca2b89b166ab1b57f5fb20cf6d3f402a0bec50b1`.
