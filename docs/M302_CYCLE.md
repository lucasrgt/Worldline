# M302 qualification cycle

`ShovelSoftBreaksCycle` rebuilds the raised stone fixture in two fresh
official server JVMs. Each run places dirt `3`, sand `12`, gravel `13`,
and clay `82` on the top face, holds gold shovel item `284`, fully breaks
each cell, and observes Packet21 drops. The signal must name block ids
`3`, `12`, `13`, and `82`. One official EOF is retried after a 5 second
sleep.

The frozen semantic SHA-256 is
`83e1acd8df0e978483bdfe1199d46021b2f5b8a4908c646ca1045c002e7228d9`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.

Run directly with:

```text
java tools/smoke/ShovelSoftBreaksCycle.java m302-shovel-soft-breaks
```
