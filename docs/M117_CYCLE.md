# M117 qualification cycle

`RedstoneWireDepowerCycle` verifies the official server artifact, compiles the
published API, adapter and smoke, and runs two fresh official worlds with
actor/reload session pairs.

Each actor rebuilds M116's exact column, wire and side lever. It first requires
the exact powered `9/15` precondition, then activates the lever a second time
and requires `1/0`. The fresh Packet51 must match, the complete-chunk delta
must contain exactly two states, and both worlds must produce identical signal,
trace and signature values. Diagnostic mode cannot qualify.

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`87c06977c34465cb580ba9a857102c62e6953ede7cfe339c2730fc9673a699fe`.
