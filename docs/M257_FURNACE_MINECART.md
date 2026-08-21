# M257 furnace minecart

M257 opens the official furnace-minecart object boundary. Packet15 of
furnace-minecart item `343` on raised rail `66:0` creates one EntityMinecart
of furnace kind. Two connected clients receive the identical Packet23
type-`12` spawn with thrower `0` and a fixed-point pose at the rail center
(`x+0.5`, `y+0.85` floored through `*32`).

Frozen semantic SHA-256:
`57acb0174de88e73ae6725e8a676aa2dffb0d4b73fe19cbe462be5d882a70264`.

This type is distinct from M155 empty-minecart type `10` and M256
chest-minecart type `11`. The cycle reuses `RemoteObjectSpawn` and
`awaitObjectSpawn`; it does not add a second Packet23 tracker.

This milestone does not claim powered-rail motion, furnace-cart fuel or
push, riding, derail, collision, or persistence of the cart across restart.
