<!-- worldline-map-schema=1 -->
<!-- boundary=state-world-differential -->
<!-- nonclaims=arbitrary-entities,arbitrary-targets,ai-target-selection,movement-execution -->
<!-- frozen-trace=7d60a218116c3281ab77011768f14b4237d0b92b81f3e0d99cdb6fabb085029a -->

# M622 pathfinding matrix behavior map

## Boundary

This milestone invokes the Beta 1.7.3 server pathfinder directly for the same
pig, start coordinate, target coordinate, range, and three in-memory terrain
fixtures. The open fixture has no obstruction. The detour fixture places a
two-block-high wall across the direct route with one bounded gap. The sealed
fixture encloses the target inside a two-block-high stone ring.

Every returned path node is serialized in milliblocks. The open route must
reach the target without leaving its lane, the detour must reach the target
through the wall gap, and the sealed route must terminate outside the ring.

## Mapping anchors

- `World` maps to client `fd` and server `dj`;
  `getEntityPathToXYZ(Entity, int, int, int, float)` is `a` on both sides.
- `PathEntity` maps to client `dh` and server `cb`; `isFinished()` is `b`,
  `getPosition(Entity)` is `a`, and `incrementPathIndex()` is `a`.
- `Vec3D` maps to client `bt` and server `ba`; coordinates are `a`, `b`, and
  `c` in both artifacts.
- `EntityPig` maps to client `wh` and server `oc`.

## Oracle independence

The mapped subject compiles against RetroMCP's mapped server classes. The
official oracle uses only obfuscated symbols and compiles directly against the
hash-verified official server JAR. They share only `CanonicalTrace`, the seed,
and literal fixture geometry.

## Pass condition

Two fresh mapped processes and two fresh official-oracle processes must be
deterministic within each pair and byte-identical across the mapping boundary.
Each case must satisfy its route invariant and remain within 64 nodes.

This milestone does not claim arbitrary entity dimensions, target selection,
movement execution, attack AI, doors, water, lava, ladders, or terrain outside
the three frozen fixtures.

## Frozen semantic signal

`official oracle: MATCH`
