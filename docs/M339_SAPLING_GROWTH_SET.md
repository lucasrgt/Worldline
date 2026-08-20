# M339 sapling growth set

M339 opens the official multi-damage sapling growth compound. Isolated
dirt pads on the raised stone fixture carry oak sapling `6:0`, spruce
sapling `6:1`, and birch sapling `6:2`. Packet15 of bonemeal dye `351:15`
calls official `BlockSapling.growTree` for each species. The frozen
oracle is the three root transitions `6:0→17:0`, `6:1→17:1`, and
`6:2→17:2` plus the same cells after a clean save and fresh login.

A failed generate restores the sapling, including official stage bit `8`.
The smoke retries bonemeal and then waits a bounded official random-tick
window. Exact canopy shape and wait length are not hashed.

This milestone is distinct from M140, which grows one oak sapling, and
from M305, which jumps wheat age and waits cactus plus sugar-cane height.
It does not claim giant trees, leaf decay, drops, or a Worldline tree
simulator.

Frozen semantic SHA-256:
`cbb09ab44fa0804f8304e414f683a868c16aabac0c29c00ba78b525e6678ec5e`.
