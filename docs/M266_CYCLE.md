# M266 qualification cycle

`CookedFishEatCycle` rebuilds the cooked-fish `350` fixture in two fresh
official server JVMs. Each run air-uses one cooked fish, heals Packet8
`15 -> 20`, consumes the stack, and reloads that inventory plus health.

A transient official EOF sleeps five seconds and retries one fresh workspace.

The frozen semantic SHA-256 is
`6a35349bc3363e2a0bdcba540cf2da951f99fef652b0ebf654ed56e15f0e168f`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless `B173WireClient` only; no GUI and no Aero.
