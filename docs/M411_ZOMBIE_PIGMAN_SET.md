# M411 zombie pigman set

M411 qualifies the official Beta 1.7.3 dedicated-server Nether zombie pigman
family as one compound SET. A dimension `-1` player seed logs in through the
M130 / M359 Nether profile (`allow-nether=true`). Packet15 places a default
mob spawner `52` on netherrack `87`. After a clean save the DIM-1 region NBT
`EntityId` is rewritten from `Pig` to `PigZombie`.

Official `EntityList` maps class `pi` to name `PigZombie` and Packet24 type
`57`. `EntityPigZombie.j` (`getDropItemId`) returns `Item.ap`
(`porkchopCooked`), item constructor `64` plus `256`, legacy id `320`. Gold
nuggets (`371`) do not exist in Beta 1.7.3.

The frozen signal names type `57` and drop `320` together with Nether
habitat `-1`. This is distinct from M388's Overworld zombie `54` feather
`288` plus skeleton `51` arrow `262`.

Frozen semantic SHA-256:
`c448868efb22d1c2a50bab9554f6c30de3f184d9cc2eb129103068be9868ae84`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
