# M552 TNT QC set

M552 opens the official TNT quasi-connectivity prime set. Packet15
places TNT block `46:0` on a raised stone support, then a solid stone
cell on top of that TNT. Packet15 of lever item `69` on the east face
of the upper stone, then empty-hand Packet15, powers the block above
the TNT cell. That removes block `46:0` and emits Packet23 type `50`
on the existing object tracker. After the bounded fuse, Packet60
destroys the constructed support and leaves an air crater that
survives a clean save plus fresh login.

The frozen signal includes TNT `46:0->0:0`, lever `69:1->9` on the
block above, Packet23 type `50`, and crater persistence. This is
distinct from shipping M219 (unprimed place only), M137 (Packet60
detonate without a Packet23 prime oracle), and M381 (flint-and-steel
prime). It does not place redstone on the TNT cell and does not add a
second Packet23 tracker.

This milestone does not claim exact blast rays, a deterministic
destroyed-cell count, chained TNT, entity damage, knockback, or fire
spread. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`a0ad8d6262175c29d1c7d1dadfcaf90f6a45d1db92c4c7dbbb63983a969b0732`.
