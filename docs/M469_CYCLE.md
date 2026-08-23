# M469-VOID-DEATH-SET Void death set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M469 is a compound official void-death SET. One empty `VoidDeath469`
starts in underside void air above the kill plane. Packet13 walks down
in steps of at most 9 until pose `y` is below 0 and then below `-64`.
Packet8 health reaches `0`. Packet9 then returns the actor to overworld
dimension `0` at health `20`, which persists after a clean save.

This is distinct from M135, which seeds already under the kill plane and
waits. It does not claim M135 player respawn from mob or lava, M461 fall
damage above the void, or M465 environmental death. Headless
`B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`52332cdbcd2108c4f8baa59811bffe40d9ba676283c851371bb2bee321f7ef98`.

## Qualification cycle

`VoidDeathSetCycle` walks underside void air in two fresh official
server JVMs. Each run starts `VoidDeath469` above the kill plane with
pose `y` already below 0, then Packet13-walks down with a movement cap
of 9 until Packet8 health is `0`. Packet9 restores overworld spawn
health `20`. One official EOF is retried after a 5 second sleep.
Headless `B173WireClient` is the only client. There is no GUI and no
Aero path.

TestKit parity is the semantic evidence boundary: `BehaviorExpectation`
compares implementation evidence with this same `void-death` Atlas token,
signal, and signature. It does not replace or re-run the official-JAR cycle.

Run the complete isolated qualification with:

```text
java tools/harness/Gate.java --milestone m469-void-death-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`52332cdbcd2108c4f8baa59811bffe40d9ba676283c851371bb2bee321f7ef98`.

Expected signal: `walk-off=cap9,steps=7,pose-y<0,health=20->0->20,packet8=0,packet9=09:00,dimension=0,spawn-y>=0,persisted=20,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `52332cdbcd2108c4f8baa59811bffe40d9ba676283c851371bb2bee321f7ef98`.
