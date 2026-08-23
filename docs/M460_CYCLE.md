# M460 qualification cycle

`MonsterBedInterruptSetCycle` rebuilds a raised grass-and-bed fixture with
one MobSpawner in two fresh official server JVMs. Each run places item
`355`, retargets the spawner to `Zombie`, advances world time to night
`14000`, occupies the bed through Packet15, observes Packet17 sleep enter,
then waits until an arena-contained Packet24 type `54` hostile interrupts sleep so
the occupied head `26:12` returns to `26:8`. Packet70Bed stays `-1` (not
rain). Morning skip is rejected. One official EOF is retried after a 5
second sleep. A 24-block fence perimeter prevents spawned attackers from
leaving the raised platform.

Run directly with:

```text
java tools/smoke/MonsterBedInterruptSetCycle.java m460-monster-bed-interrupt-set
```

The frozen semantic SHA-256 is
`fa58ada55be2832285f313973cf389f37a678482be07caf247515cebc8e150af`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
