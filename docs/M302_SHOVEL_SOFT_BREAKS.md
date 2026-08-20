# M302 shovel soft breaks

M302 opens the official gold-or-iron shovel harvest boundary across the
four soft blocks. Packet15 places dirt `3`, sand `12`, gravel `13`, and
clay `82` on a raised stone support. Packet14 while holding gold shovel
item `284` fully breaks each cell to air and drops Packet21 stacks for
each harvest. The frozen signal names those four block ids.

This milestone is distinct from M223 dirt, M239 sand, M218 gravel, and
M204 clay place persistence, and from sibling M324 gold-shovel dirt
alone. It does not claim grass, farmland `60`, hoe till, durability, or
falling physics. Headless `B173WireClient` protocol-14 only. No GUI. No
Aero.
