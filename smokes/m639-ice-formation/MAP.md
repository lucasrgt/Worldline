<!-- worldline-map-schema=1 -->
<!-- boundary=ice-formation -->
<!-- nonclaims=client-precipitation-rendering,arbitrary-biomes,flowing-water,successful-rng-pass -->
<!-- frozen-trace=2ad98fbac1c8766b2665d8845e6383c0d6b3d6ff91789292082490926020eba1 -->

# M639 semantic map

Evidence: official-server dual replica.

The public boundary is `worldline.testkit.IceFormationFixture#verify`. It advances paired
ambient passes, requires both observations to remain in a snow-enabled biome, accepts ice only
below block light ten, and requires the light-fifteen control to remain source water. Evidence
contains the attempt ceiling and stable before/after/light boundary, not the successful draw.

The mapped path subclasses `World` and invokes protected
`updateBlocksAndPlayCaveSounds()`. The official path subclasses server `dj` and invokes its
protected counterpart `j()`. Both execute the native random selection, biome query, top-solid
height, metadata-zero water check, and block replacement; neither calls a direct ice placement.

Seed `1772835215` is bounded to the active 19-by-19 chunk radius. In-memory chunks provide the
same source-water plane to both lanes. The high-light lane sets only block-light storage before
the scheduler begins. Four processes must emit one identical canonical trace.

Frozen signal: `official oracle: MATCH`.

Frozen semantic SHA-256: `2ad98fbac1c8766b2665d8845e6383c0d6b3d6ff91789292082490926020eba1`.
