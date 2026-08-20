# M408 qualification cycle

`SquidInkSetCycle` rebuilds the seed-water surface dock in two fresh
official server JVMs. Each run enables animal spawning, waits a bounded
window for Packet24 type `94` in water, and kills it with diamond sword
`276`. Packet21 must include ink sac `351:0`. One official EOF is retried
after a 5 second sleep. Headless `B173WireClient` is the only client. There
is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/SquidInkSetCycle.java m408-squid-ink-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`4f3c68e6439036720158970ea6fb62f2db5d9bb980f42850dbb0cfdf53ac0f41`.
