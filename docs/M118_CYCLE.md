# M118 qualification cycle

`IronDoorPowerCycle` verifies the official server artifact, compiles the
published API, adapter and smoke, and runs two fresh official worlds with
actor/reload session pairs.

Each actor constructs the exact ten-stone column, places item 330 as the two
door blocks, attaches a side lever, and requires the stable `1/0/8` baseline.
Activation must yield `9/4/12`; a fresh Packet51 must match; the full-chunk
delta must contain exactly the lever and two door halves. Both worlds must
produce identical signal, trace and signature values. Diagnostic mode cannot
qualify.

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`e2000f240f0dce5e5fe233611cca6053e50b31c57113fd564387a00f527d7573`.
