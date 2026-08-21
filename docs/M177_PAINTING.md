# M177 painting place

M177 opens the official painting-spawn boundary. Item `321` used through
Packet15 on the west face of a raised 2x2 stone wall causes protocol-14
Packet25. Two headless peers observe the same entity identity and title.

Official Packet25 is entityId int, title UTF-16 string, then x, y, z, and
direction ints. This is not Packet23. Art titles such as Kebab or Aztec
are chosen by the official server RNG; they match across the two peers in
one JVM and are not hashed when they diverge across JVMs.

This milestone does not claim painting break, persist, or other Packet25
arts as a hashed sequence.
