# M402 remaining detector rail

M402 opens the official remaining detector-rail SET. Packet15 of detector rail
item `28` on a raised stone support first places unpowered `28:0`. Packet15 of
minecart item `328` on that detector emits Packet23 type `10` through the
existing object tracker, with thrower `0` and a fixed-point pose at the
detector center. Occupancy bit 8 then writes `28:8`. One cycle includes both
unpowered and cart-powered states.

The frozen signal includes `28:0->8` and Packet23 type `10`. That occupied
cell remains after a clean save plus fresh login.

This milestone is distinct from unpowered detector place `28:0` (M185) and from
powered-rail `27` torch power (M309). It does not re-qualify powered-rail
motion launching a cart onto a detector (M377). It does not add a second
Packet23 tracker. It does not claim boats, chest or furnace carts, riding,
derail, collision, or redstone wire.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`00ed23852b2822be0b8b8766debc5cf5049c7e54b7c106f0e7c8d6a5028b8ab3`.
