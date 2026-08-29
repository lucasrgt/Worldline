# B173-MOB-SPAWNER-SUBSYSTEM-CONFORMANCE-CYCLE official Beta 1.7.3 mob-spawner subsystem conformance

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

One public TestKit mini-subsystem maps mob-spawner block 52 across the block, ItemBlock, and TileEntityMobSpawner registries. It proves native item placement and default Pig delay, finite removal with no item drop, Zombie EntityId plus Delay chunk-NBT recovery, an unscheduled block with a range-gated tile tick, and stable ordinary neighbor notifications. Existing physical envelopes, native rendering, spawned-mob identities, spawn geometry, and randomized delay reset evidence remain independently owned and are not count-farmed here.

## Qualification cycle

Run executes two fresh mapped worlds and two fresh untouched official-server worlds. Every replica inspects all three registry surfaces, places item 52 through the native ItemBlock route, applies the server harvest sequence, reloads Zombie delay 37 through native chunk NBT, contrasts an out-of-range stable delay with one nearby-player decrement, and applies stone and lever neighbor notifications through MobSpawnerSubsystemFixture.

Expected signal: `family=mob-spawner-subsystem,subject=52,claims=7,registry=block+item+tile,item-placement=52x1>0,break=finite+removed,drop=none,persistence=Zombie:37,ticks=far20+near19,neighbors=stable,oracle=MATCH`.

Frozen semantic SHA-256: `59bee5c5d91affa8c14f6444cc85de3e1b4cb3c94fb2fd0f68d4b106897a2a46`.
