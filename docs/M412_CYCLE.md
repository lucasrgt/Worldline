# M412 qualification cycle

`SlimeSplitSetCycle` rebuilds one official slime-chunk spawner in two fresh
official server JVMs. Each run enables monsters, places spawner `52` below
`y=16` in chunk `-2,-2`, retargets `EntityId` to `Slime`, kills Packet24
type `55`, and requires child type-`55` Packet24 spawns after parent death.
One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/SlimeSplitSetCycle.java m412-slime-split-set
```

The frozen semantic SHA-256 is
`04232de5b9eb6e2e741dbbf008ade42638370d907b361856800b70fe8cb6e59b`.

Canonical evidence uses two official server JVMs and six client sessions.
Headless protocol-14 only. No GUI. No Aero.
