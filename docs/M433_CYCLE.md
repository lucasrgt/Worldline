# M433 qualification cycle

`RemainingChestOrientSetCycle` rebuilds the raised stone row in two fresh
official server JVMs. Each run places isolated chest `54` with look yaw
`-90` and `90`, then an east-west pair and a north-south pair, and reloads
all six `54:0` cells. The signal must include those look yaws plus both
pair axes and must not claim the M349 large window. One official EOF is
retried after a 5 second sleep. Headless `B173WireClient` is the only
client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/RemainingChestOrientSetCycle.java m433-remaining-chest-orient-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`b9750e81a03028d1bb7345d6699d951772dea723fefb2cb303312f4c43423f03`.
