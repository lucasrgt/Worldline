# M274 qualification cycle

`FallingGravelCycle` verifies the official server artifact, compiles the
published API, adapter and smoke, and runs two fresh official worlds with
actor/reload session pairs. Headless `B173WireClient` is the only client.
There is no GUI and no Aero path. One official EOF is retried after a
5 second sleep.

Each actor builds the exact column, establishes stable stone-below-gravel
state, removes the stone support through Packet14, and requires transient
air before the official gravel settlement. The fresh Packet51 must match,
the full-chunk delta must contain exactly the source and destination
cells, and both worlds must produce identical gravity, trace and
signature values. Diagnostic mode cannot qualify.

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`176ae1fac3a1eb0fc755149f750defb1e9bf184c097416e0d6f216e41c7fb222`.
