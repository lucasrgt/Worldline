# M263 stew eat

M263 opens the official `ItemFood` mushroom-stew boundary. Stew item `282`
is seeded into the hotbar with health `12` via `B173PlayerSeed.writeInventory`.
Packet15 air-use (direction `255` at `-1,255,-1`) eats that stack: Packet8
health restores `12 -> 20` (eight points, capped at 20) and Packet103 replaces
the held stack with bowl `281`. Beta 1.7.3 has no hunger bar; stew heals
health and leaves the bowl.

This is distinct from bread `297` (heal 5, consume to empty), cookie `357`
(heal 1, consume to empty), and cooked pork `320` (heal 8, consume to empty).
Stew is `ItemSoup` and always leaves bowl `281`.

This milestone does not claim bread, cookie, pork, apple, golden apple,
fish, cake, crafting, or hunger-era food.
