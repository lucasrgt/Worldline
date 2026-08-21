# M124 cross-chunk light recovery

Status: GO in Worldline v1.112.0.

M124 closes the lifecycle opened by M123. After an independent client captures
the exact lit state, Packet14 removes glowstone from `(15,55,6)`. Packet53
observes air, official water updates restore `9:0`, and a final client receives
complete snapshots of chunks `(0,0)` and `(1,0)`.

All 55 source-chunk and 19 neighbor-chunk light changes reverse. The source and
water samples return from 15 and 12 to zero, and both baseline-to-final delta
hashes are the SHA-256 of an empty sequence.

M124 proves one cross-chunk removal/recovery path. It does not claim arbitrary
light sources, materials, directions, simultaneous mutations, unloaded chunks,
tick-exact latency, skylight recovery, or a Worldline lighting implementation.
