<!-- worldline-map-schema=1 -->
<!-- boundary=b173-mob-spawner-subsystem-conformance-cycle -->
<!-- nonclaims=state-domain,collision-shape,light-behavior,native-render,spawned-mob-identity,spawn-geometry,randomized-delay-reset -->
<!-- frozen-trace=59bee5c5d91affa8c14f6444cc85de3e1b4cb3c94fb2fd0f68d4b106897a2a46 -->

# Beta 1.7.3 mob-spawner subsystem conformance

`MobSpawnerSubsystemFixture` maps block `52` across three connected native registry surfaces:
`BlockMobSpawner`, its automatically registered `ItemBlock`, and `TileEntityMobSpawner`. Placement
through item `52` consumes the supplied stack, creates block state `52:0`, and materializes the
default `Pig` tile with delay `20`.

The block has finite player-relative break strength, transitions to air, and emits no item entity.
Native chunk NBT preserves block state `52:0` together with a deliberately selected `Zombie`
`EntityId` and delay `37`.

Block `52` is not enrolled in the block scheduler. Its tile tick is range-gated: delay `20` remains
unchanged without a nearby player and decrements to `19` after a player joins inside sixteen
blocks. Stone and lever neighbor callbacks preserve both the block and its default `Pig:20` tile.

The Functional Census profile is enriched to `mob-spawner,tick-driven,tile-entity`. State domain,
collision, and light remain owned by `b173-tile-utility-physical-envelope-cycle`; native rendering
remains owned by `m703-native-3d-inventory-render`. Existing entity-specific, spawn-geometry, and
random-delay-reset cycles remain separate and are not count-farmed here.

Frozen aggregate signal:
`family=mob-spawner-subsystem,subject=52,claims=7,registry=block+item+tile,item-placement=52x1>0,break=finite+removed,drop=none,persistence=Zombie:37,ticks=far20+near19,neighbors=stable,oracle=MATCH`.

Qualified semantic signature: `59bee5c5d91affa8c14f6444cc85de3e1b4cb3c94fb2fd0f68d4b106897a2a46`.
