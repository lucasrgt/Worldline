# M430 remaining painting motives

M430 opens the official remaining Packet25 motive-size family. Item `321`
used through Packet15 on raised 4x2, 4x3, and 4x4 west-face stone walls
causes three protocol-14 Packet25 spawns. Those surfaces admit Fighters,
Skeleton/DonkeyKong, and Pointer/Pigscene/BurningSkull, which cannot fit
M177/M351's 2x2 wall. Two headless peers observe the same entity identities.

Official Packet25 is entityId int, title UTF-16 string, then x, y, z, and
direction ints. This is not Packet23. Art titles are chosen by the official
server RNG; they match across the two peers in one JVM and are not hashed
when they diverge across JVMs. Spawned art must fit the clicked wall.

The frozen semantic SHA-256 is
`1504c14913948dca32f92c0dacff830c42a51f7c402354b7a872fc92af410e09`.

This milestone is distinct from M351's west-face plus east-face orientation
set. It does not claim painting break, persist, or hashed RNG titles.
Headless `B173WireClient` only. No GUI. No Aero.
