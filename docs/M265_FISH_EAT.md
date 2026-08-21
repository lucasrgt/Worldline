# M265 fish eat

M265 opens the official `ItemFood` raw-fish boundary. Raw fish item `349` is
seeded into the hotbar with health `18` via `B173PlayerSeed.writeInventory`.
Packet15 air-use (direction `255` at `-1,255,-1`) eats that stack: Packet8
health restores `18 -> 20` (two points, capped at 20) and the held stack is
consumed `349:1 -> empty`. Beta 1.7.3 has no hunger bar; raw fish heals
health.

This is distinct from cooked fish item `350`. Cooked fish is a separate
`ItemFood` with heal `+5` and is not eaten here.

This milestone does not claim cooked fish, bread, cooked pork, apple, golden
apple, cookie, mushroom stew, cake, or hunger-era food.
