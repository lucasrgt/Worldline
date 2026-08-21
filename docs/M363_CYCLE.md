# M363 qualification cycle

`HostileIdentitySetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places two default spawners `52`, retargets
the saved MobSpawner `EntityId` values to `Zombie` and `Skeleton`, sets
night, and requires Packet24 types `54` and `51`. One official EOF is
retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/HostileIdentitySetCycle.java m363-hostile-identity-set
```

The frozen semantic SHA-256 is
`e6df497cd2826b04e3930ffb08caa875bba470b29a8b5bad4ce5cc75d48db14d`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
