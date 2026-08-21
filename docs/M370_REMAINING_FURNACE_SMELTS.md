# M370 remaining furnace smelts

M370 is the remaining official furnace-smelt set after M296, M324, and M338.
One cycle places three idle furnaces `61:2` on a raised stone fixture and
smelts cactus `81` to cactus green `351:2`, oak log `17` to charcoal `263:1`,
and clay ball `337` to brick `336`. Each recipe uses coal `263` and
Packet100/102/103/105 evidence.

Public furnace-load identities still reject cactus, log, and clay. This
milestone keeps those Packet102 clicks smoke-local and adds no public API.
It is distinct from M296 iron `15`, gold `14`, and pork `319`, and from M324
sand `12`, cobble `4`, and fish `349`. It does not claim pickup, experience,
alternate fuels, or burning furnace `62` as a placement product. Headless
`B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`912452d315840ced68811ccce77f3cde4f1250eac7068c5ddd9f85e22a607a2a`.
