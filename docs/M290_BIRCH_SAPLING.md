# M290 birch sapling

M290 opens the official birch-sapling placement boundary. Packet15 of
sapling item `6` with damage `2` cannot stay on stone; Beta 1.7.3
`BlockSapling` extends `BlockFlower` and requires dirt, grass, or farmland.
Dirt `3` is therefore placed on the M175 raised stone fixture, and the
sapling is planted on that dirt as `6:2`. That exact cell remains after a
clean save plus fresh login.

This milestone is distinct from M202 oak `6:0` and M289 spruce `6:1`. It
does not wait for tree growth and does not claim bone meal.

The frozen semantic SHA-256 is
`21f35395f38d2877297a2801023c0e7e0e0b5fc83a8ec278dee1ad7b7151b8a0`.
