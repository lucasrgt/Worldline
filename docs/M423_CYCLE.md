# M423 qualification cycle

`SlimeballSetCycle` rebuilds one official slime-chunk spawner in two fresh
official server JVMs. Each run enables monsters, places spawner `52` below
`y=16` in chunk `-2,-2`, retargets `EntityId` to `Slime`, kills Packet24
type `55` until Packet21 slimeball `341` appears, and crafts sticky piston
`29` from piston `33` plus slimeball `341`. One official EOF, actor death,
or expected-block miss is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/SlimeballSetCycle.java m423-slimeball-set
```

The frozen semantic SHA-256 is
`8a525200f72521e3f129de58b27232e197c39cf2d41da689d2772e9a830ac411`.

Canonical evidence uses two official server JVMs and six client sessions.
Headless protocol-14 only. No GUI. No Aero.
