# M365 behavior map

The server half seeds compass item `345`, Packet16-selects it, reads
`level.dat` `SpawnX/Y/Z`, observes two distinct player cells, saves, and proves
the item plus fixture through a fresh login. Its trace claims only server spawn
data, held-item state, positions, and persistence.

The needle half requires a separate client differential through actual mapped
and official `TextureCompassFX` execution. `B173CompassPoint` only reads spawn
coordinates and player cells; it contains no needle formula.

Replacement signatures remain pending official qualification. Compass
crafting, clock behavior, map use, GUI behavior, and Nether spin are excluded.
