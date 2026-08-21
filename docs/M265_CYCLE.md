# M265 qualification cycle

`FishEatCycle` rebuilds the raw-fish `349` fixture in two fresh official
server JVMs. Each run air-uses one raw fish, heals Packet8 `18 -> 20`,
consumes the held stack, and reloads that inventory plus health.

A transient official EOF sleeps five seconds and retries one fresh workspace.

The frozen semantic SHA-256 is
`0c9b15289f11f60a602735efc2cf64ae7cf2e4ad6454e33fd5fdb6a44023f832`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless `B173WireClient` only; no GUI and no Aero.
