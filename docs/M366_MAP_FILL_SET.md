# M366 map fill set

M366 opens the official empty-map air-use boundary. Empty map item `358`
is seeded into the hotbar via `B173PlayerSeed.writeInventory`. Packet15
air-use (direction `255` at `-1,255,-1`) is the fill attempt. The official
Beta 1.7.3 dedicated server does not change that stack on protocol-14:
held remains `358:1:0 -> 358:1:0`. The same empty map persists across a
clean save plus fresh login.

This is distinct from M325. M325 crafts compass `345`, clock `347`, and
empty map `358` through Packet102 workbench take. M366 never opens a
workbench and never claims a crafted result.

Frozen semantic SHA-256:
`048613204222ae9dce7fb157d74dc94b69573ce8faaa9dd90cff64f7aab8f31f`.

This milestone does not claim map pixels, Packet131 map data, compass or
clock GUI, or any post-Beta-1.7.3 empty-map item split. Headless
`B173WireClient` only. No GUI. No Aero.
