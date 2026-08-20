# M260 apple eat

M260 opens the official apple-food boundary. Apple item `260` is seeded with
health `16` through `B173PlayerSeed.writeInventory`. Packet15 air-use
(direction `255`) eats that stack: Packet8 health restores `16 -> 20` (four
points, cap 20) and Packet103 consumes the apple.

Beta 1.7.3 has no hunger bar; apples heal health instantly. Official food
`maxStackSize` is 1. This milestone does not claim golden apple `322`, cake,
crafting, or hunger-era food.
