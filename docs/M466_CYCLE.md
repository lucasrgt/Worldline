# M466 qualification cycle

`SquidLandDeathSetCycle` rebuilds the seed-water surface dock, a `7×7`
stone floor, and a dry open `5×5` `y<63` pen in two fresh official server JVMs.
Each run places one default
spawner `52`, retargets the saved MobSpawner `EntityId` to `Squid`, waits
for Packet24 type `94` on land, and observes out-of-water Packet38 status
`3` plus Packet29. Both server phases explicitly enable vanilla animals;
semantic misses fail immediately, while one official EOF is retried after a
5 second sleep. The actor reloads inside the open dry pen. It accepts only a dry squid
within official attack range, and follows already-received movement without
blocking on movement packets. Headless `B173WireClient`
is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/SquidLandDeathSetCycle.java m466-squid-land-death-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`3c3b628471c4ee01b5da67ea523767d75fcc305a6747025b28720ca05ecab8a6`.
