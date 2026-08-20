# M267 milk bucket

M267 opens the official milk-bucket use boundary. Milk item `335` is seeded
into the hotbar with health `20` via `B173PlayerSeed.writeInventory`. Packet15
air-use (direction `255` at `-1,255,-1`) drinks that stack against a raised
stone basin floor: Packet103 replaces the held item `335:1:0 -> 325:1:0`.
Health stays `20 -> 20`. Beta 1.7.3 has no status effects; milk does not
heal. The basin cell stays air.

This is distinct from M168 water pickup and M181 lava pickup. Those empty
bucket `325` uses hit still water `9` or still lava `11`. Milk never writes
or removes a fluid cell.

This milestone does not claim cow milking, cake crafting, water buckets,
lava buckets, or hunger-era potion clearing.
