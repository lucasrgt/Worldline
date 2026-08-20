# M384 cactus sugar set

M384 opens the official cactus-and-sugar-cane compound. Packet15 of cactus
item `81` places block `81:0` on isolated sand `12:0`. Packet15 of reed item
`338` places cane `83:0` on grass `2:0` beside still water `9:0`. Official
random ticks then grow at least one cactus and one cane from height `1` to
height `>= 2`. A fresh login rereads both stacks.

This is distinct from M159 (single water-adjacent cane), M167 (single cactus
placement with no height wait), and M305 (wheat bonemeal plus cactus and cane).
It does not claim harvest, bone meal, height 3, sand cane planting, or a
Worldline crop simulator. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`ebe81626228e8dc034975562ddc312713b9877d4020a97cec9b6e38884191824`.
