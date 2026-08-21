# M22 Multiplayer Wire Boundary

Two fresh Worldline scenario JVMs each start one unmodified official Beta 1.7.3
dedicated server and one original Worldline wire client. The client performs the
native protocol-14 handshake and offline login over a localhost socket. The
server's native `list` command must show exactly the configured username; after
the client socket closes, `list` must become empty before server shutdown.

The client parses the official login response and records a nonnegative entity
ID, but the exact ID, port, timing, seed-derived spawn, and packets after login
remain observational. M22 does not use or emulate the official graphical
client, move a player, or claim packet-stream determinism.

Frozen expected signature SHA-256: `723f96819bd972ec5f2a4d932251840099f2d6472edf590c4386641a7d7e08f9`
