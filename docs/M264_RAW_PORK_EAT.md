# M264 raw pork eat

M264 opens the official `ItemFood` raw-porkchop boundary. Raw porkchop item
`319` is seeded into the hotbar with health `17` via `B173PlayerSeed.writeInventory`.
Packet15 air-use (direction `255` at `-1,255,-1`) eats that stack: Packet8 health
restores `17 -> 20` (three points, capped at 20) and the held stack is consumed
`319:1 -> 0`. Beta 1.7.3 has no hunger bar; raw pork heals health.

This is distinct from M259 cooked pork item `320`, which heals eight points,
and from M150 pig pork drop, which observes Packet21 loot rather than eating.

This milestone does not claim cooked pork, bread, apple, golden apple, cookie,
mushroom stew, fish, cake, or hunger-era food.
