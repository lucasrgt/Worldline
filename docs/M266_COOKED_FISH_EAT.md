# M266 cooked fish eat

M266 opens the official `ItemFood` cooked-fish boundary. Cooked fish item
`350` is seeded into the hotbar with health `15` via
`B173PlayerSeed.writeInventory`. Packet15 air-use (direction `255` at
`-1,255,-1`) eats that stack: Packet8 health restores `15 -> 20` (five
points, capped at 20) and the held stack is consumed `350:1 -> 0`. Beta
1.7.3 has no hunger bar; cooked fish heals health.

This is distinct from raw fish item `349`. Raw fish is a separate
`ItemFood` with heal `+2` and is not eaten here.

This milestone does not claim raw fish, bread, cooked pork, apple, golden
apple, cookie, mushroom stew, cake, or hunger-era food.
