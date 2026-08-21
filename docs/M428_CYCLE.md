# M428 qualification cycle

`RemainingDoorOrientSetCycle` rebuilds the raised stone 2x2 in two fresh
official server JVMs. Each run places wooden door item `324` with look yaw
`-90`, `0`, `90`, and `180`, then reloads lower/upper pairs `64:0/8`,
`64:1/9`, `64:2/10`, and `64:3/11` after save plus fresh login. The signal
must include those four upper+lower `64:8` family pairs and must not claim
iron door `71` or trapdoor `96`. One official EOF is retried after a 5
second sleep. Headless `B173WireClient` is the only client. There is no
GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingDoorOrientSetCycle.java m428-remaining-door-orient-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`10dad6f6b34f4140a80e7a09abeebaa5ff502bc6eee4607964a64dae72626bd2`.
