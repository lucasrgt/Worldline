# M412 qualification cycle

`SlimeSplitSetCycle` rebuilds one official slime-chunk spawner in two fresh
official server JVMs. Each run enables monsters, places spawner `52` below
`y=16` in chunk `-2,-2`, retargets `EntityId` to `Slime`, kills Packet24
type `55`, and requires child type-`55` Packet24 spawns after parent death.
Full diamond armor is seeded only as a bounded-fixture safety control. One
official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/SlimeSplitSetCycle.java m412-slime-split-set
```

The frozen semantic SHA-256 is
`c35f4196ac8b8633b7ffa097ac87eca1828f89b9ab9d2be20f0802c924191929`.

Canonical evidence uses two official server JVMs and six client sessions.
Headless protocol-14 only. No GUI. No Aero.
