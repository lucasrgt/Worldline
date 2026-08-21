# M21 Dedicated Server Control

M21 promotes the first neutral dedicated-server API:

- `DedicatedServerRuntime.boot()` starts a configured server;
- `setTime(long)` sends the bounded time command;
- `save()` waits for the native save-complete marker;
- `state()` returns immutable lifecycle, port, online-mode, persisted time, and
  completed-save observations;
- `close()` sends the native stop command and requires a clean exit.

The b1.7.3 adapter starts the unmodified hash-pinned official server. It writes
only an isolated `server.properties`, communicates over standard input/output,
and reads the generated gzip/NBT `level.dat` with original Worldline code.

## Executable proof

Two fresh controller JVMs each start a fresh official server, wait for native
readiness, issue `time set 6000`, force `save-all`, read the persisted `Time`
tag, and stop cleanly. The gate bounds persisted time to `[6000, 6200)` because
the unmodified server remains live between command and save. The qualifying run
persisted exactly 6000 in both processes.

## Non-claims

The server tick thread is not paused or externally stepped. M21 has no connected
client and does not claim packet, login, or multiplayer determinism. It proves
process lifecycle, bounded commands, save completion, and persisted observation.
