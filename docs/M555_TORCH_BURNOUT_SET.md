# M555 torch burnout set

M555 opens the official redstone-torch burnout boundary. Packet15 of
redstone torch item `76` on the north face of an unpowered raised stone
places lit wall torch `76:4`. One lever invert plus a neighbor-block update
proves the Packet53 `76 <-> 75` family. Rapid lever Packet15 toggles then
perform 24 rapid activations. The torch holds
unlit `75:4` while the support is unpowered, which is the burnt state, and
recovers to `76:4` after a 400-tick wait and later neighbor update. This
fixture does not measure an internal eight-toggle threshold or 100-tick
window. The recovered cell remains after a clean save plus fresh login.

Semantic SHA-256: pending final serialized qualification.

This milestone is distinct from M312's single invert (`76:4 -> 75:4` while
the support stays powered) and from M182 floor torch `76:5`. It does not
claim wire consumers, lighting, or a generic redstone model. Headless
`B173WireClient` protocol-14 only. No GUI. No Aero.
