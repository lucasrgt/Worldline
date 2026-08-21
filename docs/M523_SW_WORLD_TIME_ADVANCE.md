# M523-SW persisted world-time advance

M523-SW freezes the official Beta 1.7.3 server clock boundary. A clean world's
offline NBT `Time` long is patched to `3000000000` and restarted. One
protocol-14 client then sustains
80 official heartbeats before another save. The persisted gzip-NBT `Time` value
must advance within a bounded server-tick window and remain above 32-bit range.

A second restart observes the saved value before issuing any heartbeat and then
saves immediately. Its smaller bounded delta is the negative control. Frozen
evidence therefore depends on ordering, persistence, NBT long width, and clean
save/restart behavior rather than scheduler-exact timing.

The test profile enables the Nether so both official world servers tick and
the later secondary-dimension save is not a stale canonical snapshot. M500
separately freezes the disabled-Nether `level.dat`/`level.dat_old` save order.

This milestone does not claim cross-dimension clock synchronization, weather,
gamerules, spawn-cycle, bed-skip, or a client clock.
