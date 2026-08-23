<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=4a1d5934a7e4543cf2631661ef6408677022ac1247d30f1d9b1135e4a5a0a6c7 -->

# M515-SW behavior map

## Boundary

This candidate compares fire support loss directly across mapped b1.7.3
classes and the hash-verified official server JAR. Each in-memory world uses a
stone support at `(8, 64, 8)` and fire `51:0` at `(8, 65, 8)`. `World.rand` is
fixed to seed `51520240820`.

Three fixtures isolate the rule. The positive fixture places supported fire
and removes its stone support through `setBlockWithNotify`. The negative keeps
the support. The mutation removes the support before attempting to place fire.
The support and fire ID/metadata pairs are captured before and after the
fixture action and through two bounded `World.tick()` calls.

## Mapping anchors

- `World` maps to client `fd` and server `dj`; `rand` is `r`, `tick()` is
  client `l` / server `h`, `setBlockWithNotify` is client `f` / server `e`,
  and `setBlockAndMetadataWithNotify` is `b` on both sides.
- `Block` maps to client `uu` and server `na`; `stone` is `u`, `fire` is `as`,
  and `blockID` is `bn`.
- `BlockFire` maps to client `yq` and server `pt`. Its inherited
  `canPlaceBlockAt` and `onNeighborBlockChange` entry points are `a` and `b`.

## Pass condition

Two fresh mapped runs and two fresh official-oracle runs must be deterministic
inside each pair and byte-identical across the mapping boundary. Supported fire
must exist before the action. Removing support must leave both cells as air;
retaining support must keep stone and fire; starting unsupported must reject
the fire and remain air through the bounded ticks.

This milestone does not claim an exact fire lifetime, spread, lava ignition,
rain behavior, fuel consumption, Nether persistence, packets, GUI behavior,
or persistence. Its frozen trace signature is
`4a1d5934a7e4543cf2631661ef6408677022ac1247d30f1d9b1135e4a5a0a6c7`.

## Frozen semantic signal

`oracle=MATCH,fixture=m515-sw-fire-support-extinguish,ticks=2,controlled=true`
