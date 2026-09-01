<!-- worldline-map-schema=1 -->
<!-- boundary=flowing-fluid-lifecycle -->
<!-- nonclaims=gameplay-placement,break-transition,drop-matrix,native-render,nether-lava-odd-metadata -->
<!-- frozen-trace=f21fb7eb6a04dcf14e8fa7195c4e66f478e83c93f8d59548abe36a59a62b9de9 -->

# Beta 1.7.3 flowing-fluid lifecycle map

## Boundary

The public `FlowingFluidLifecycleFixture` exercises moving water `8` and moving lava `10`
inside native official-server worlds. Moving controls are installed without neighbor notification
and observed before entering the scheduler. Stone-lined cascades then derive horizontal and falling
metadata, while separate gated channels prove exact first-flow cadence and still-to-moving neighbor
recomputation.

The water cascade covers metadata `0..15`. The Overworld lava cascade covers the even domain
`0,2,4,6,8,10,12,14`; its census state-domain claim remains partial because the Nether-only odd
levels belong to the independently owned Nether flow-rate contract. Both moving blocks expose a
passable collision envelope and their native opacity, emission, block-light, and skylight values.
The qualified source cell records water as opacity/emission/block-light/skylight `3/0/0/12` and
lava as `255/15/15/0`; the opaque lava-table value is a Beta 1.7.3 runtime observation rather than
a value inferred from later Minecraft versions.
The first generated horizontal cells are saved while still moving and recovered through a fresh
`World` over the same native chunk-loader boundary.

## Causality

- `setBlockAndMetadata` creates only the initial moving controls and never notifies neighbors.
- `scheduleUpdateTick` is invoked only after the initial controls have been observed.
- `setBlockWithNotify` opens each stone gate beside a settled still source.
- `World.tick` owns propagation, metadata derivation, and still-to-moving transitions.
- `World.saveWorld` owns chunk persistence; the loader snapshots native `Chunk` IDs and metadata.
- The public fixture rejects cadence, domain, collision, lighting, neighbor, or reload drift.

## Nonclaims

This package does not claim direct item placement of moving blocks, break or drop behavior, native
client rendering, fluid reactions, source regeneration, entity current effects, or the odd lava
metadata reachable under Nether cadence.

Frozen semantic signature: `f21fb7eb6a04dcf14e8fa7195c4e66f478e83c93f8d59548abe36a59a62b9de9`.

Frozen semantic signal:
`oracle=MATCH,fixture=b173-flowing-fluid-lifecycle-cycle,ticks=240,controlled=true`.
