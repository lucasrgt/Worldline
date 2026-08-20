# M289 spruce sapling

M289 opens the official spruce-sapling placement boundary. Packet15 of
sapling item `6` with damage `1` cannot stay on stone; Beta 1.7.3
`BlockSapling` extends `BlockFlower` and requires dirt, grass, or
farmland. Dirt `3` is therefore placed on the M175 raised stone fixture
as in M198, and the sapling is planted on that dirt as `6:1`. That exact
cell remains after a clean save plus fresh login.

This milestone is distinct from M202 oak sapling `6:0`. It does not wait
for tree growth and does not claim birch `6:2` or bone meal (M140 already
covers that).
