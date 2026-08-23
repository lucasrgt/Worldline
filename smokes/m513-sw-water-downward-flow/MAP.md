# M513-SW behavior map

## Boundary

This candidate compares scheduled vertical water flow directly across mapped
b1.7.3 classes and the hash-verified official server JAR. Each in-memory world
contains a one-cell-wide stone-lined column with flowing water `8:0` placed at
`(8, 68, 8)`. `World.rand` is fixed to seed `51320240820`, and exactly 60
`World.tick()` calls advance vanilla scheduled block updates.

Three fixtures isolate the geometry: an open column ending on the ordinary
Y=64 stone floor, a blocked column with stone immediately below the source,
and a mutation shaft that removes the floor through Y=61. Every tick records
world time plus exact ID and metadata pairs from Y=68 through Y=61.

## Mapping anchors

- `World` maps to client `fd` and server `dj`; `rand` is `r`, `tick()` is
  client `l` / server `h`, `setBlockAndMetadataWithNotify` is `b` on both
  sides, and `getWorldTime` is client `t` / server `m`.
- `IBlockAccess.getBlockId` is `a` on both sides; `getBlockMetadata` is client
  `e` / server `c`.
- `Block` maps to client `uu` and server `na`; `waterMoving` is `B`,
  `waterStill` is `C`, `stone` is `u`, and `blockID` is `bn`.

## Pass condition

Two fresh mapped runs and two fresh official-oracle runs must be deterministic
inside each pair and byte-identical across the mapping boundary. The source
must remain water (`8` or `9`). The open fixture must carry water down to Y=65
without replacing its Y=64 stone floor; the blocked fixture must retain stone
at Y=67 and air at Y=66; the floor-removed shaft must carry water through Y=64
to Y=61. Exact intermediate IDs, metadata, and cadence come only from the
frozen oracle trace.

This milestone does not claim horizontal spread, source regeneration, entity
push, fluid reactions, packets, persistence, or dimension-specific behavior.
Its frozen trace signature is
`c8cd76aa79d46ffdecd0dbabdff860b95de7b1c6cce4783dbf4ea796a9bc41ee`.

## Frozen semantic signal

`oracle=MATCH,fixture=m513-sw-water-downward-flow,ticks=60,controlled=true`
