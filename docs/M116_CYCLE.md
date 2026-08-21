# M116 qualification cycle

`RedstoneWirePowerCycle` verifies the official server artifact, compiles the
expanded neutral interaction interface, adapter and smoke, then runs two fresh
official worlds with actor/reload session pairs.

Each actor builds the exact column, uses item 331 to create wire 55, places the
side lever, and requires the stable `1/0` lever/wire baseline. After activation
it requires `9/15`, exactly two complete-chunk state changes, and an identical
fresh Packet51. Both rows, traces and signatures must match before frozen
evidence is checked. Diagnostic mode cannot qualify.

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`973fb75a9541e4f8015d8133d7c99779e6c1ab8b6ef095120609e6a6fcab5587`.
