# M68 Aero Multiplayer Login

M68 connects a real Minecraft Beta 1.7.3 client modified by Fabric,
StationAPI, and Aero 3.0.0 to the hash-verified official dedicated server.
The server is unmodified, local, and offline-mode; the client uses the
production `ConnectScreen` path rather than Worldline's protocol adapter.

## Qualified boundary

The test-only overlay fixes one username and localhost endpoint, observes the
official Packet1 login handler, the first Packet13 play-ready transition, and
a Packet51 only after vanilla applies it. Once the remote world, named player,
network handler, and multiplayer interaction manager agree, it counts twenty
completed renderer updates and requires a parseable Aero frame-log row written
after that readiness boundary.

The overlay then disconnects the client network handler and schedules normal
client shutdown. The runner independently requires the official server login,
lost-connection, and clean-stop evidence. The Aero checkout revision and clean
status are checked before and after two fresh scenarios.

## Scope

The Aero test mod remains loaded for its diagnostics, but its custom content is
disabled. M68 therefore proves that StationAPI and Aero compose with a vanilla
remote world and real renderer; it does not prove synchronized Aero blocks or
entities. It also does not claim pixel equivalence, FPS, deterministic frame
duration, a lag spike, combat, causal attribution, online authentication,
headless operation, reconnect, or arbitrary remote/modded servers.
