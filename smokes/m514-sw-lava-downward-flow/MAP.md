<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=701ed9ab8e197906a0a9e3737dce82ad359f8152f4867def70e44a94144fca4c -->

# M514-SW behavior map

## Boundary

This candidate compares scheduled vertical lava flow directly across mapped
b1.7.3 classes and the hash-verified official server JAR. Each in-memory world
contains a one-cell-wide stone-lined column with flowing lava `10:0` placed at
`(8, 68, 8)`. `World.rand` is fixed to seed `51420240820`, and exactly 240
`World.tick()` calls advance vanilla scheduled block updates in the Overworld.

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
- `Block` maps to client `uu` and server `na`; `lavaMoving` is `D`,
  `lavaStill` is `E`, `stone` is `u`, and `blockID` is `bn`.

## Pass condition

Two fresh mapped runs and two fresh official-oracle runs must be deterministic
inside each pair and byte-identical across the mapping boundary. The source
must remain lava (`10` or `11`). The open fixture must carry lava down to Y=65
without replacing its Y=64 stone floor; the blocked fixture must retain stone
at Y=67 and air at Y=66; the floor-removed shaft must carry lava through Y=64
to Y=61. Exact intermediate IDs, metadata, and Overworld cadence come only
from the frozen oracle trace.

This milestone does not claim ignition, horizontal flow, Nether behavior,
source regeneration, entity effects, packets, persistence, or fluid reactions.
Its frozen trace signature is
`701ed9ab8e197906a0a9e3737dce82ad359f8152f4867def70e44a94144fca4c`.

## Frozen semantic signal

`oracle=MATCH,fixture=m514-sw-lava-downward-flow,ticks=240,controlled=true`
