# M469 qualification cycle

`VoidDeathSetCycle` walks underside void air in two fresh official
server JVMs. Each run starts `VoidDeath469` above the kill plane with
pose `y` already below 0, then Packet13-walks down with a movement cap
of 9 until Packet8 health is `0`. Packet9 restores overworld spawn
health `20`. One official EOF is retried after a 5 second sleep.
Headless `B173WireClient` is the only client. There is no GUI and no
Aero path.

Run directly with:

```text
java tools/smoke/VoidDeathSetCycle.java m469-void-death-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`52332cdbcd2108c4f8baa59811bffe40d9ba676283c851371bb2bee321f7ef98`.
