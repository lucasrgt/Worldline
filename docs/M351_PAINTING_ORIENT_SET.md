# M351 painting orientation set

M351 opens the official compound painting-orientation boundary. Item
`321` used through Packet15 on two raised 2x2 stone walls causes two
protocol-14 Packet25 spawns. The west face and east face produce distinct
facing values. Two headless peers observe the same entity identities.

Official Packet25 is entityId int, title UTF-16 string, then x, y, z, and
direction ints. This is not Packet23. Art titles such as Kebab or Aztec
are chosen by the official server RNG; they match across the two peers in
one JVM and are not hashed when they diverge across JVMs.

The frozen semantic SHA-256 is
`8f60b715dc6a3aeab49aaae89f1f147dd7822ab37806a8da79597e86acd2e9aa`.

This milestone clones M177's single west-face painting and does not claim
painting break, persist, or other Packet25 arts as a hashed sequence.
Headless `B173WireClient` only. No GUI. No Aero.
