# M202 sapling

M202 opens the official oak-sapling placement boundary. Packet15 of oak
sapling item `6` cannot stay on stone; Beta 1.7.3 `BlockSapling` extends
`BlockFlower` and requires dirt, grass, or farmland. Dirt `3` is therefore
placed on the M175 raised stone fixture, and the sapling is planted on
that dirt as `6:0`. That exact cell remains after a clean save plus fresh
login.

The frozen semantic SHA-256 is
`7772115ec090ef211b01204fa558371ea9983994367b0ceb0899a44441bdb24d`.

This milestone does not wait for tree growth and does not claim bone meal
(M140 already covers that).
