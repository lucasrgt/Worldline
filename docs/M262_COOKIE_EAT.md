# M262 cookie eat

M262 opens the official `ItemFood` cookie boundary. Cookie item `357` is seeded
into the hotbar with health `19` via `B173PlayerSeed.writeInventory`. Packet15
air-use (direction `255` at `-1,255,-1`) eats that stack: Packet8 health
restores `19 -> 20` (one point, capped at 20) and the held stack is consumed
`357:1 -> empty`. Beta 1.7.3 has no hunger bar; cookie heals health.

This is distinct from M160 cake bite and M258 bread. Cake places item 354 as
BlockCake `92` and uses empty-hand Packet15 on that block. Bread item `297`
heals five points. Cookie never places a food block.

This milestone does not claim cooked pork, apple, golden apple, bread,
mushroom stew, fish, cake, or hunger-era food.
