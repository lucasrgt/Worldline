# M411 qualification cycle

`ZombiePigmanSetCycle` rebuilds a Nether netherrack-and-spawner fixture in
two fresh official server JVMs. Each run logs a dimension `-1` actor, places
block `52`, retargets the saved MobSpawner `EntityId` to `PigZombie`, observes
Packet24 type `57`, kills with Packet7 diamond sword `276`, and requires
Packet21 cooked pork `320`. The frozen signal must name type `57`, Nether
`-1`, and drop `320`. One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/ZombiePigmanSetCycle.java m411-zombie-pigman-set
```

The frozen semantic SHA-256 is
`c448868efb22d1c2a50bab9554f6c30de3f184d9cc2eb129103068be9868ae84`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
