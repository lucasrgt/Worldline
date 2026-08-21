# M312 torch invert

M312 opens the official redstone-torch invert boundary. Packet15 of redstone
torch item `76` on the north face of an unpowered raised stone places lit
wall torch `76:4`. Powering that block through a west-facing repeater inverts
the same cell to unlit `75:4`. Both IDs are observed in one cycle. The
inverted cell remains after a clean save plus fresh login.

This milestone is distinct from M182 floor torch `76:5`. It does not claim
wire consumers or the lighting plane. Headless `B173WireClient` only. No
GUI. No Aero.
