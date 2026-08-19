# M167 cactus

M167 opens the official cactus-placement boundary. A raised isolated stone
column receives sand `12:0`. Packet15 of cactus item `81` places block `81:0`
in the air cell above that sand. The cactus remains after a 40-tick live hold
and after a clean save plus fresh login.

Beta 1.7.3 `canBlockStay` requires sand or cactus below and no solid horizontal
neighbor. The raised column is the fixture that satisfies that rule. The
frozen `81:0` is the placement identity; random-tick age clocks are not hashed.

Frozen semantic SHA-256:
`9a210a58a09a40ac501c31bf8262bee7846ea1240c7dc0654766374ba627ef30`.

This milestone does not claim growth to height 2 or 3, breaking, adjacent-block
pop, or smelting cactus to green dye.
