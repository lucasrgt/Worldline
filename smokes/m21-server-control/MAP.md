# M21 Dedicated Server Control Boundary

Two fresh Worldline controller JVMs each start one unmodified official Beta
1.7.3 dedicated server through the neutral `DedicatedServerRuntime` API and the
`b173-server` process adapter. Each controller waits for readiness, sets world
time to 6000, forces a save, reads the persisted `Time` value from `level.dat`,
and closes the server cleanly.

The persisted time is bounded to `[6000, 6200)` because the native server keeps
ticking after the command. The exact time and startup duration are
observational; the frozen trace records only accepted command, completed save,
bounded persisted state, and clean shutdown.

M21 does not patch the server, pause its tick thread, connect a client, or claim
network determinism. JARs, generated worlds, properties, and logs remain under
ignored roots.
