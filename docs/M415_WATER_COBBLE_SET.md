# M415 water cobble set

M415 opens the official compound flowing-lava cobble set. Still lava `11:0`
sits in two raised stone trenches behind dirt gates. Packet14 opens the
adjacent cells to air. Official scheduled flow then publishes flowing lava
(moving block `10`, Packet53 stationary-flow `11:2`) into those cells.
Packet15 places still water `9:0` beside each flowed cell. Vanilla neighbor
processing hardens each flowing lava cell to cobblestone `4:0` because the
flow metadata is greater than `0`. Both lava sources stay lava sources.
The frozen signal names flowing lava `10`, water `9`, and cobble `4` across
two cobble cells.

This is distinct from shipping M139 (still lava `11:0` plus water to
obsidian `49`) and from M414 (still-lava obsidian set). It does not claim
obsidian, buckets, vertical stone generation, or a Worldline fluid
simulator. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`bf5ec9eaf7f4f9ec7cf8652c8bdef0af40a1d8fa89b618d519dc571fddc66148`.
