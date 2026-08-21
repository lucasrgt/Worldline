# M405 qualification cycle

`PigSaddleSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run places one default spawner `52`, waits for
Packet24 type `90`, and uses saddle item `329` with Packet7 button 0.
Saddle consume plus Packet39 attach of that player onto the pig is the
SET. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/PigSaddleSetCycle.java m405-pig-saddle-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`a27d2ce0c705f4fe5af56c8e35b8ec7c212956eaff46a764ce610d54f40c06d9`.
