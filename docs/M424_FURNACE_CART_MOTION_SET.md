# M424 furnace cart motion set

M424 opens the official compound furnace-cart-motion SET. Furnace-minecart
item `343` on regular rail `66` and coal item `263` run as one family.
Packet15 places `66:0` on a raised stone support with a north stone wall and
a second `66:0` cell one cell south. Packet15 of furnace-minecart `343` on
the first rail emits Packet23 type `12` through the existing object tracker,
with thrower `0` and a fixed-point pose at that rail center. A bounded live
hold proves the unfueled cart does not launch; detector `28:0` stays idle.
Packet7 of coal `263` then consumes the stack and pushes the cart south away
from the player on rail `66`. The cart occupies the detector and bit 8 writes
`28:8`.

The frozen signal includes `type12`, `66:0`, `coal=263:1->0`,
`unfueled-hold=idle`, `fueled=1`, and `28:0->8` occupancy after motion on
rail `66`. Those exact cells remain after a clean save plus fresh login.

This milestone is distinct from M257 spawn-only type `12` (no coal, no motion)
and from M377 powered-rail type `10` motion (item `328` on `27:8`). It does
not add a second Packet23 tracker. It does not claim riding, chest carts,
powered rail `27`, derail, collision, or redstone wire.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`536398b8e8c64ca3dc8e527842ae556bf4175363fc0b8e554d2ba0ec52811b1b`.
