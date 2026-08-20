# M453 player death drops set

M453 opens the official player-death plus multi-item-drop family. Actor
`DeathDrop453` is seeded below the world with hotbar stone `1`, cobble `4`,
and dirt `3`. Vanilla void damage drives Packet8 health from `20` to `0`.
The official server then emits Packet21 for those three distinct seeded
item ids. Void death is the most deterministic of lava, void, and mob.

This is distinct from M50 Packet14 drop-current and from a single item
drop. It does not claim M388 zombie feather `288` / skeleton arrow `262`,
M444 pig pork `319` / sheep wool `35:0`, M135 Packet9 respawn except as
needed to finish a session, or XP (none in Beta 1.7.3). Headless
`B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`6d7e55c8c86f1540d7306a507b0a07af3ef9cbe3b6f6c79cf2b87663beab7ed0`.
