# M381 TNT prime set

M381 opens the official compound TNT prime set. Packet15 places TNT
block `46:0` on a raised stone support. Packet15 of flint-and-steel
item `259` on that cell removes the block and emits Packet23 type `50`
on the existing object tracker. After the bounded fuse, Packet60
destroys the constructed support and leaves an air crater that
survives a clean save plus fresh login.

The frozen signal includes TNT `46:0->0:0`, flint `259`, Packet23 type
`50`, and crater persistence. This is distinct from shipping M219
(unprimed place only), M137 (Packet60 detonate without a Packet23
prime oracle), and M343 (flint-and-steel fire family). It does not
add a second Packet23 tracker.

This milestone does not claim exact blast rays, a deterministic
destroyed-cell count, chained TNT, entity damage, knockback, or fire
spread. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`6cf1cfe074d14a3c856cf768c9a8b9cdc9cfa573b8ee2e901445db31692bfad5`.
