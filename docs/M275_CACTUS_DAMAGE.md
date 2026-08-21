# M275 cactus damage

M275 opens the official cactus-collision damage boundary. It clones the M167
raised sand fixture: sand `12:0` then cactus item `81` as block `81:0`. After
that cell is live, the headless actor moves into the cactus AABB.

Vanilla Beta 1.7.3 `BlockCactus.onEntityCollidedWithBlock` deals one unarmored
hit. Packet38 status 2 precedes Packet8 health `20 -> 19`. Health 19 persists
after a clean save plus fresh login.

This is distinct from M167 cactus placement. M167 only proves the planted
`81:0` cell remains. M275 freezes the health transition from contact.

Frozen semantic SHA-256:
`c708ae878b6079760d5c246f952ca1789d98c31e395a568ad9c1a2d751ef6df8`.

This milestone does not claim cactus growth, breaking, adjacent-block pop,
armor reduction, repeated hits, death, or fire/lava damage.
