# M111 qualification cycle

`FixedSeedTerrainCycle` verifies the official dedicated-server byte length,
SHA-1 and SHA-256, compiles the protocol-14 adapter and smoke, then runs two
fresh server JVMs, client sessions and world directories.

Each client completes the initial play exchange, consumes Packet50/Packet51
until absolute chunk `(0,0)` is decoded, validates the full `16 x 128 x 16`
shape and nonempty columns, and closes cleanly. The runner requires both runs
to match chunk coordinate, non-air count, surface digest, trace, and signature.
Each full block-ID hash is retained as diagnostic evidence and may differ.

Canonical evidence:

- servers: 2 fresh official JVMs;
- clients: 2 fresh protocol-14 sessions;
- non-air blocks per chunk: 16,342;
- full block-ID SHA-256: diagnostic per run;
- surface SHA-256: `bb5fa0b1c2f242c7952ec1e58d269d66705fa308fcdcc3b25a30c8b309ea74db`.

Diagnostic mode cannot qualify. The frozen semantic SHA-256 is
`b885d60be98dfb11c60f51a928c0fa9bdda225520187692098587a72e253fa98`.
