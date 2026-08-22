# M365 behavior map

Frozen expected signature SHA-256: 45694ff4da19a7b3c554d405380e9535c075d7fee1f3880674813ed5cc9a8b68

The server half seeds compass item `345`, Packet16-selects it, reads
`level.dat` `SpawnX/Y/Z`, observes two distinct player cells, saves, and proves
the item plus fixture through a fresh login. Its trace claims only server spawn
data, held-item state, positions, and persistence.

The needle half requires a separate client differential through actual mapped
and official `TextureCompassFX` execution. `B173CompassPoint` only reads spawn
coordinates and player cells; it contains no needle formula.

Replacement signatures remain pending official qualification. Compass
crafting, clock behavior, map use, GUI behavior, and Nether spin are excluded.
