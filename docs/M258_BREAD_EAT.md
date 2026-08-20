# M258 bread eat

M258 opens the official `ItemFood` bread boundary. Bread item `297` is seeded
into the hotbar with health `15` via `B173PlayerSeed.writeInventory`. Packet15
air-use (direction `255` at `-1,255,-1`) eats that stack: Packet8 health
restores `15 -> 20` (five points, capped at 20) and the held stack is consumed
`297:1 -> 0`. Beta 1.7.3 has no hunger bar; bread heals health.

This is distinct from M160 cake bite. Cake places item 354 as BlockCake `92`
and uses empty-hand Packet15 on that block. Bread never places a food block.

This milestone does not claim cooked pork, apple, golden apple, cookie,
mushroom stew, fish, cake, or hunger-era food.
