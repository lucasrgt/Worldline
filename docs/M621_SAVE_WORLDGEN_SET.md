# M621-SAVE-WORLDGEN-SET save worldgen set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M621 closes the replay-safe save/chunk and bounded worldgen gaps for one fixed 3x3 Beta 1.7.3 region. It validates all nine McRegion location, timestamp, sector, zlib, and NBT-root frames; proves the geology census survives a clean save/restart; and requires multiple surface families, subsurface cave air, and at least three ore components.

## Qualification cycle

DataDrivenCycle executes two fresh official-server replicas. Each replica generates and settles the exact region, validates its McRegion frames, restarts the same saved world, and compares the bounded geology, surface-family, cave-air, and ore-component census. The proof does not claim arbitrary seeds, biome names, structure generation, or mutable entity/tick state.

Expected signal: `region=31:33:31:33,chunks=9,mcr=9-valid-zlib-nbt,restart=geology-equal,biomes=surface-families>=2,caves=subsurface-air>0,ores=blocks>0+veins>=3,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `269269a06fb4fbd3833715a554c0830bbc3bd6c70e6e2db13348ac5bde2bad10`.
