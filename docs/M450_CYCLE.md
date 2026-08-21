# M450 qualification cycle

`PigmanAngerSetCycle` rebuilds a Nether netherrack pad with two pigman
spawners in two fresh official server JVMs. Each run logs a dimension `-1`
actor, places two blocks `52`, retargets both saved MobSpawner `EntityId`
values to `PigZombie`, observes two Packet24 type `57` identities, hits one
with Packet7 diamond sword `276`, and requires Packet38 status `2` plus
neighbor aggro. The frozen signal must name two type `57` identities, Nether
`-1`, and neighbor anger. It must not name pork `320` or type `90`. One
official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/PigmanAngerSetCycle.java m450-pigman-anger-set
```

The frozen semantic SHA-256 is
`ae24558c960284894ed1577e583f5fbbdcfd65ebfd4ed48af6687179d2ccf098`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
