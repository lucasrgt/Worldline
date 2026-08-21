# M438 qualification cycle

`RemainingClockMapSetCycle` rebuilds the workbench fixture in two fresh
official server JVMs. Each run crafts clock `347`, Packet16-holds it,
Packet15 air-uses empty map `358`, and reloads both stacks after save
plus fresh login. The frozen signal names clock `347` hold plus the
official unfilled result `358:1:0->358:1:0`. One official EOF is retried
after a 5 second sleep. Headless `B173WireClient` is the only client.
There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingClockMapSetCycle.java m438-remaining-clock-map-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`9ebe2cca746ab29d741407b8788d0b10a7e942cd691b868eb0d1d2f00e83eb58`.
