# M459 qualification cycle

`GhastFireballHitSetCycle` rebuilds a Nether netherrack-and-cobble platform
in two fresh official server JVMs. Each run logs a dimension `-1` actor,
waits for Packet24 type `56` and Packet23 type `63`, then requires that
fireball's Packet60 strength `1` hit: Packet8 hurt and/or a netherrack or
cobble crater. One official EOF is retried after a 5 second sleep. Player
moves stay at or below 9 blocks. Lava is not credited as the hit.
The cavern scan uses only frozen support chunk `2,-1`, so both replicas choose
the same fixture regardless of loaded-chunk iteration order.

Run directly with:

```text
java tools/smoke/GhastFireballHitSetCycle.java m459-ghast-fireball-hit-set
```

The frozen semantic SHA-256 is
`491a34451873fea634086ff4a8c83a68e25ff5a8ed43d75033d4ed22b63f5042`.

Canonical evidence uses two official server JVMs and three client sessions.
Headless protocol-14 only. No GUI. No Aero.
