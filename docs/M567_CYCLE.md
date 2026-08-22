# M567 qualification cycle

`BedSpawnSetCycle` rebuilds a raised 3x3 stone-and-bed fixture in two fresh
official server JVMs. Each run places item `355`, advances world time to
night through the existing lab console `time set`, occupies the bed through
Packet15, observes Packet17 sleep enter and occupied bit `4`, leaves/wakes
standing, then contacts cactus `81` until Packet8 health is `0`. Packet9
respawns at the bed, not `level.dat` world spawn. One official EOF is
retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/BedSpawnSetCycle.java m567-bed-spawn-set
```

The frozen semantic SHA-256 is
`aaad061b562df911b0b4c29784fe2beb4b0d5f1183dae8e29603cd3c2a838aed`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
