# M119 qualification cycle

`FallingSandCycle` verifies the official server artifact, compiles the
published API, adapter and smoke, and runs two fresh official worlds with
actor/reload session pairs.

Each actor builds the exact column, establishes stable stone-below-sand state,
removes the stone support through Packet14, and requires transient air before
the official sand settlement. The fresh Packet51 must match, the full-chunk
delta must contain exactly the source and destination cells, and both worlds
must produce identical gravity, trace and signature values. Diagnostic mode
cannot qualify.

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`ac00ec1900fdfc0489c6e7d4e9621c916411505d522df3c1fc9f3c53a78eb656`.
