# M569-SPAWNER-DELAY-SET Spawner delay set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M569 opens the official mob-spawner delay and activation-range SET. One
saved mob spawner `52` is retargeted from `Pig` to `Zombie` and its NBT
`Delay` is rewritten to `1`. After midnight (`time set 14000`) a headless
protocol-14 client 24 blocks above the spawner must not observe Packet24
type `54` for a bounded 40-tick wait. Stationing inside the 16-block
activation range must then publish Packet24 type `54`.

This is distinct from M141's type-`90` pig identity, M390's creeper type
`50` plus spider type `52` identity, and M564 spawn light. Delay/range is
the claim, not living-type identity.

The frozen semantic SHA-256 is
`f8a3a81f8f2295adbcd12af662bb00620eea7f2d0b09701089362e062b5d0b19`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

## Qualification cycle

`SpawnerDelaySetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places one default spawner `52`, retargets
the saved MobSpawner `EntityId` to `Zombie`, rewrites `Delay` to `1`, sets
night, requires Packet24 type `54` to stay absent while the actor is 24
blocks above the spawner, then requires type `54` after the actor stations
in range. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/SpawnerDelaySetCycle.java m569-spawner-delay-set
```

The frozen semantic SHA-256 is
`f8a3a81f8f2295adbcd12af662bb00620eea7f2d0b09701089362e062b5d0b19`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,platform=7x7-48grass,spawner=4:72:4:52:0,entityid=Zombie,delay=1,far=24,wait=40,absent=true,near=type54,night=14000,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `f8a3a81f8f2295adbcd12af662bb00620eea7f2d0b09701089362e062b5d0b19`.
