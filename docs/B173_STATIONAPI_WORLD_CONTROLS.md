# B173-STATIONAPI-WORLD-CONTROLS-CYCLE StationAPI native local-world controls

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

The reviewed structural capability WorldlineContract.STATIONAPI_WORLD_CONTROLS serves three reusable consumers: block fixture setup, player fixture setup, and inventory GUI acceptance. The public WorldControlFixture exercises real block ID/metadata reads and writes, no-op results, teleport, hotbar selection, native item insertion and census, inventory screen topology, native slot clicks and item conservation. All control acknowledgements are session- and sequence-bound and must leave the controlled tick unchanged. This driver package adds no finite gameplay Census cells; its structural capability extends the TestKit runtime surface.

## Qualification cycle

Two fresh Fabric/StationAPI clients use the pinned official Beta client as their runtime base and distinct world directories using the installed StationAPI storage provider. The explicit local-world profile runs the native singleplayer world and interaction manager, so controls act on the authoritative world rather than a remote client mirror. The game-thread channel performs and acknowledges each operation before returning. Opening the inventory completes a native GUI frame synchronously, without advancing world time. Public acceptance compares 19 equatable observations from each process, proves zero command ticks and exactly one requested native tick, rejects invalid positions/hotbar slots and stale or foreign-session UI nodes, and validates two sealed native profiler captures. Protocol regression rejects wrong sessions, replayed or future request numbers and malformed acknowledgements. The legacy multiplayer profile retains its independently qualified lifecycle; these controls require the local-world profile. This does not claim multiplayer authoritative writes, extended StationAPI block IDs beyond the public Beta BlockState range, arbitrary mod GUIs, keyboard/pointer/screenshot support, or full world item/block censuses. No official source or assets are distributed.

Expected signal: `provider=stationapi-b1.7.3,profile=local-world,sessions=2,testkit=2-pass,atoms=3,isolation=fresh-client+world,profiler=2-sealed-wlpr`.

Frozen semantic SHA-256: `67d63d5b713f90f0c8efe9049d3a14f110d8ebdb521be2b0ab186d357352a048`.
