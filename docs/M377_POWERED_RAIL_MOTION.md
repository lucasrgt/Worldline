# M377 powered rail motion

M377 opens the official compound powered-rail-motion SET. Powered rail `27`,
detector rail `28`, and minecart item `328` run as one family. Packet15 places
unpowered `27:0` on a raised stone support with a north stone wall. Packet15
places detector `28:0` one cell south. Packet15 of minecart `328` on that
powered rail emits Packet23 type `10` through the existing object tracker,
with thrower `0` and a fixed-point pose at the powered-rail center. A bounded
live hold proves the unpowered rail does not launch the cart; detector `28:0`
stays idle. Packet15 of floor torch `76:5` then sets powered bit 8 (`27:8`).
The cart launches onto the detector and occupancy bit 8 writes `28:8`.

The frozen signal includes `27:0->8`, `28:0->8`, `unpowered-hold=idle`, and
Packet23 type `10` spawned on the powered rail rather than the detector.
Those exact cells remain after a clean save plus fresh login.

This milestone is distinct from M309 rail-power place (cart spawned on the
detector for occupancy, no motion) and from M310 vehicle-rides spawn/attach
(Packet39). It does not add a second Packet23 tracker. It does not claim
riding, chest or furnace carts, derail, collision, or redstone wire.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`c383cb26d4289064f7ced386bb9c7cfc9cdb68545275f438464e17ef5a161977`.
