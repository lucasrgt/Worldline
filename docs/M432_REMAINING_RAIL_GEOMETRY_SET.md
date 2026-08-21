# M432 remaining rail geometry set

M432 qualifies the official Beta 1.7.3 dedicated-server remaining rail `66`
curve and slope metadata family as one compound SET. Packet15 of rail item
`66` on a raised stone column plus an east step places ascending-east slope
`66:2`. A two-block-south L of three rails writes south-east curve `66:6`.
The frozen signal names both remaining geometry cells. Both survive a clean
save plus fresh login.

This family is distinct from shipping M183 (one north-south `66:0` rail),
M309 rail power (`27:8` and detector `28:8`), M377 powered-rail motion, and
M402 remaining detector occupancy (`28:0->8` by cart type `10`). It does
not claim powered rail `27`, detector rail `28`, minecarts, riding, or
redstone. Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

Frozen semantic SHA-256:
`3da03f5b4d6dd509fa5fc0925d5ea7422d5cd6ddb96e7acb84b5854de2ab61b1`.
