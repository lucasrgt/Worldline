# M380 trapdoor family set

M380 opens the official compound trapdoor-orientation family. Item `96` is
placed against a raised stone south, north, east, and west face. Empty-hand
Packet15 toggles each cell through its closed-open-closed metadata pair:
south `96:1->5->1`, north `96:0->4->0`, east `96:3->7->3`, and west
`96:2->6->2`. All four closed cells persist across a clean save plus fresh
login.

This family is distinct from shipping M163 (east place) and M306 (wooden
door close plus east trapdoor close). It does not claim redstone,
waterlogging (absent in this version), iron trapdoors (absent in this
version), or top-half attachment (absent in this version). Headless
`B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`ab78b72d72f7fa3016aff5ef1e7d1fa6d51961bb14c02d74afa5e1a5ecf036e7`.
