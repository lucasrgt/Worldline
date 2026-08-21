# M453 qualification cycle

`PlayerDeathDropsSetCycle` repeats seeded hotbar death drops in two fresh
official server JVMs. Each run seeds stone `1`, cobble `4`, and dirt `3`
at Y `-80`, waits for Packet8 health `0`, and requires Packet21 for those
ids. One official EOF is retried after a 5 second sleep. Headless
`B173WireClient` is the only client. There is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/PlayerDeathDropsSetCycle.java m453-player-death-drops-set
```

Canonical evidence uses two official server JVMs and two client sessions.
The frozen semantic SHA-256 is
`6d7e55c8c86f1540d7306a507b0a07af3ef9cbe3b6f6c79cf2b87663beab7ed0`.
