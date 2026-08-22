# M555 torch burnout set

M555 opens the official redstone-torch burnout boundary. Packet15 of
redstone torch item `76` on the north face of an unpowered raised stone
places lit wall torch `76:4`. One lever invert plus a neighbor-block update
proves the Packet53 `76 <-> 75` family. Rapid lever Packet15 toggles then
force eight turn-offs inside the official 100-tick window. The torch holds
unlit `75:4` while the support is unpowered, which is the burnt state, and
recovers to `76:4` after a later neighbor update past that window. The
recovered cell remains after a clean save plus fresh login.

Frozen semantic SHA-256:
`51a58a2129fecaba1f082e28aaa285177901ece62fb08c5e70d85fbcd3535713`.

This milestone is distinct from M312's single invert (`76:4 -> 75:4` while
the support stays powered) and from M182 floor torch `76:5`. It does not
claim wire consumers, lighting, or a generic redstone model. Headless
`B173WireClient` protocol-14 only. No GUI. No Aero.
