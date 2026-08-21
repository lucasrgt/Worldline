# M377 qualification cycle

`PoweredRailMotionCycle` rebuilds the raised stone track in two fresh official
server JVMs. Each run places powered-rail item `27`, detector rail item `28`
one cell south, and minecart item `328` on the still unpowered rail. The
unpowered hold keeps detector `28:0` idle. Floor torch `76` then powers the
rail to `27:8` and the cart activates detector `28:8`. The result is distinct
from M309 place-only rail power, M310 rides, unpowered `27:0`, unpowered
`28:0`, and regular rail `66`. One official EOF is retried after a 5 second
sleep. Headless `B173WireClient` is the only client. There is no GUI and no
Aero path.

Run directly with:

```text
java tools/smoke/PoweredRailMotionCycle.java m377-powered-rail-motion
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`c383cb26d4289064f7ced386bb9c7cfc9cdb68545275f438464e17ef5a161977`.
