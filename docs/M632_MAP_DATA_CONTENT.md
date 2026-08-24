# M632-MAP-DATA-CONTENT map data content

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M632 freezes the protocol-14 Packet131 color content of first-map item 358:0 for seed 17320110707 at stationary position 4:60:4. Because the official fallback map center follows level.dat spawn, the fixture first creates the world, stops it, and pins SpawnX/Y/Z to that same position before map creation. It reconstructs all 128 columns, waits through a complete quiet scan, and binds the resulting 128x128 color grid to a SHA-256 digest. This does not claim client rendering, map GUI appearance, dynamic player markers, arbitrary seeds or positions, map zoom, Nether maps, or post-Beta packet formats.

## Qualification cycle

DataDrivenCycle runs two fresh replicas, each with one official world-creation JVM and one official observation JVM. The creator stops cleanly before B173LevelDatSpawn changes only SpawnX/Y/Z to 4:60:4. A headless loader then receives all 49 chunks from the fixed view-distance-3 server and settles the region for 200 ticks. A second player persisted with first-map item 358:0 connects at the pinned spawn; Packet131 capture begins immediately after its handshake and before play synchronization, so no initial column is lost. B173MapDataAccess reconstructs the 128x128 grid and returns only after all columns are observed followed by 160 ticks without another color span. MapDataContentFixture publishes equatable seed, position, coverage, palette, and SHA-256 evidence. No GUI. No Aero.

Expected signal: `seed=17320110707,pos=4:60:4,map=358:0,columns=128,nonzero=697,palette=12,sha256=a09f5f7e8363e81f1258ee53714538b9513fc93e4255d31c41974c7b1297f956,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `8b54a90b94fdb4791b5892aa89c2d55b3a01fb858e24bdbc398c117ba016d373`.
