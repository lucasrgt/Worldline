<!-- worldline-map-schema=1 -->
<!-- boundary=public-testkit-pathfinding-matrix -->
<!-- nonclaims=arbitrary-entities,arbitrary-targets,ai-target-selection,movement-execution -->
<!-- frozen-trace=7d60a218116c3281ab77011768f14b4237d0b92b81f3e0d99cdb6fabb085029a -->

# M622 pathfinding matrix behavior map

## Boundary

This milestone invokes the Beta 1.7.3 server pathfinder directly for the same
pig, start coordinate, target coordinate, range, and three in-memory terrain
fixtures. The open fixture has no obstruction. The detour fixture places a
two-block-high wall across the direct route with one bounded gap. The sealed
fixture encloses the target inside a two-block-high stone ring.

Every returned path node is captured as an immutable public `PathfindingNode`
in milliblocks and grouped by `PathfindingMatrixObservation`. The reusable
`PathfindingMatrixFixture` requires the open route to reach the target without
leaving its lane, the detour to reach the target through the wall gap, and the
sealed route to terminate outside the ring. It publishes equatable,
schema-versioned `PathfindingMatrixEvidence` for downstream mod tests.

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

The public TestKit contract owns invariant interpretation; the mapped backend
only collects vanilla route nodes. The official oracle remains independent of
the TestKit and preserves the frozen differential trace.

## Pass condition

Two fresh mapped processes and two fresh official-oracle processes must be
deterministic within each pair and byte-identical across the mapping boundary.
The public fixture must accept all three mapped observations, emit stable
canonical evidence, and reject invariant drift. Each route remains bounded to
64 nodes.

This milestone does not claim arbitrary entity dimensions, target selection,
movement execution, attack AI, doors, water, lava, ladders, or terrain outside
the three frozen fixtures.

## Frozen semantic signal

`official oracle: MATCH`
