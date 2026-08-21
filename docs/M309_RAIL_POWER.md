# M309 rail power

M309 opens the official compound rail-power boundary. Packet15 of powered-rail
item `27` on a raised stone support first places unpowered `27:0`. Packet15 of
redstone torch item `76` on an adjacent east stone pad then places floor torch
`76:5` and sets powered bit 8, producing `27:8`. Two west stone pads keep
detector rail item `28` two cells away from that powered rail. Packet15 of
minecart item `328` on the detector creates one EntityMinecart. The actor
receives Packet23 type `10` with thrower `0` through the existing object
tracker, and occupancy bit 8 produces `28:8`.

The frozen signal includes both `28:8` and `27:8`. Those exact cells remain
after a clean save plus fresh login.

Frozen semantic SHA-256:
`ff3995ce5426f88877abdf561aada4f7f2968dfa7fbdc44f768202ec4c14ff80`.

This milestone is distinct from unpowered powered rail `27:0` (M184),
unpowered detector `28:0` (M185), torch-only `76:5` (M182), and minecart spawn
on regular rail `66` (M155). It does not add a second Packet23 tracker. It
does not claim minecart acceleration, chest or furnace carts, riding, derail,
or redstone wire.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
