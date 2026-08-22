# M568 qualification cycle

`ItemDespawnSetCycle` rebuilds the raised stone pad in two fresh
official server JVMs. Each run drops cobble `4` as Packet21, walks away
so Packet22 cannot collect, advances EntityItem `Age` through NBT, and
waits until the official server emits Packet29. One official EOF is
retried after a 5 second sleep.

The frozen signal must name cobble `4`, Packet29, `collect=false`, and
`age-limit=6000`. It must not collapse to M51 spawn-only, M52
collection, or M436 arrow life.

Run directly with:

```text
java tools/smoke/ItemDespawnSetCycle.java m568-item-despawn-set
```

The frozen semantic SHA-256 is
`b90bfdf125255b880fd496ce52fa92b784d5e3879fbf448482c44004bd2574f2`.

Canonical evidence uses two matching official worlds. Headless
protocol-14 only. No GUI. No Aero.
