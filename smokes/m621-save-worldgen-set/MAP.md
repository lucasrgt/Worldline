<!-- worldline-map-schema=1 -->
<!-- boundary=fixed-seed-terrain -->
<!-- nonclaims=arbitrary-seeds,biome-names,structures,mutable-entity-state -->
<!-- frozen-trace=269269a06fb4fbd3833715a554c0830bbc3bd6c70e6e2db13348ac5bde2bad10 -->

# M621-SAVE-WORLDGEN-SET save worldgen set behavior map

M621 binds a fixed 3x3 official-server region to two complementary surfaces:
the persisted McRegion frame topology and a decoded geology census after a
clean restart. Each of the nine chunks must have a valid location-table entry,
timestamp, bounded sector span, zlib payload, and NBT compound root.

The decoded census is deliberately semantic rather than a raw region-file hash.
It compares stable geology across save/restart, requires at least two surface
families as a bounded climate/biome proxy, observes subsurface air below the
surface, and counts connected ore components.

Signal: `region=31:33:31:33,chunks=9,mcr=9-valid-zlib-nbt,restart=geology-equal,biomes=surface-families>=2,caves=subsurface-air>0,ores=blocks>0+veins>=3,clients=2,disconnect=clean`.

The boundary does not claim arbitrary seeds, canonical biome names, generated
structures, or equality of mutable entities and random-tick state.

Frozen SHA-256: `269269a06fb4fbd3833715a554c0830bbc3bd6c70e6e2db13348ac5bde2bad10`.
