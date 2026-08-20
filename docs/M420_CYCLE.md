# M420 qualification cycle

`WolfTameSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places one default spawner `52`, retargets
the saved MobSpawner `EntityId` to `Wolf`, waits for Packet24 type `95`,
and uses bone item `352` with Packet7 button 0 until Packet38 status `7`.
Packet7 dye `351:4` is then used on that tamed wolf. One official EOF is
retried after a 5 second sleep. Headless `B173WireClient` is the only
client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/WolfTameSetCycle.java m420-wolf-tame-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`8268a761729c8e58ce515e8c1abb5065fa4782f824f83d9f2e6072f6e46d1833`.
