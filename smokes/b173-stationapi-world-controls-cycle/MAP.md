<!-- worldline-map-schema=1 -->
<!-- boundary=stationapi-world-controls -->
<!-- frozen-trace=67d63d5b713f90f0c8efe9049d3a14f110d8ebdb521be2b0ab186d357352a048 -->

# StationAPI world control structural capability

Public `WorldlineContract.STATIONAPI_WORLD_CONTROLS` (`stationapi-world-controls`)
binds `worldline.testkit.WorldControlFixture#exercise` and `WorldControlEvidence`.
Its reusable consumers are block fixture setup, player fixture setup, and native
inventory GUI acceptance. The game-specific operations live in the StationAPI adapter.
The local-world profile uses native singleplayer storage and interaction; the
existing remote lifecycle profile remains separately qualified.

| Atom | Native action | Equatable evidence |
| --- | --- | --- |
| Block control | World getBlockId/getBlockMeta/setBlock | Metadata round trip, no-op and removal |
| Player setup | Entity setPosition; hotbar field; PlayerInventory addStack | Exact pose, selected slots, inventory totals |
| Inventory GUI | InventoryScreen; InteractionManager.clickSlot | 45 slots, cursor-inclusive conservation, moved stack, close |

Every request is session- and sequence-bound. Commands remain within one game
thread boundary, and only TICK advances a native game tick. Bounds and stale or
foreign UI nodes fail before mutation. Two new processes and isolated world stores
exercise the same public fixture. This is reviewed structural driver coverage,
with a Functional Census delta of zero and no new vanilla gameplay behavior claim.

Frozen signature: `67d63d5b713f90f0c8efe9049d3a14f110d8ebdb521be2b0ab186d357352a048`.

Frozen signal: `provider=stationapi-b1.7.3,profile=local-world,sessions=2,testkit=2-pass,atoms=3,isolation=fresh-client+world,profiler=2-sealed-wlpr`.

Frozen trace: `schema=worldline.world-controls.v1|write=true|block=35:7|repeat=false|remove=true|air=0|pose=8.5,122.0,8.5|selected=8|selected=0|given=3|screen=inventory|slots=45|source=1:3|picked=0|cursor-inclusive=3|destination=1:3|closed=true|retained=3|command-ticks=0|native-ticks=1`.

The adapter semantic manifest binds these new sites to the existing public and native
block, pose, hotbar, inventory and GUI roles; it does not create duplicate role identities.
The isolated consumer build pins AeroModelLib revision `8a1cf47e16487d635fcf52639d55b985bbbbcdf1`.
