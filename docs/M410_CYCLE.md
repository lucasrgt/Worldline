# M410 qualification cycle

`GhastFireballSetCycle` rebuilds a Nether netherrack platform in two fresh
official server JVMs. Each run logs a dimension `-1` actor, waits for Packet24
type `56`, then discovers the live Packet23 fireball object. The frozen signal
must name type `56` and that fireball type. One official EOF is retried after
a 5 second sleep.

Run directly with:

```text
java tools/smoke/GhastFireballSetCycle.java m410-ghast-fireball-set
```

The frozen semantic SHA-256 is
`4a77f0136d56574b37e6aca69072e884a92ea9240a1904aca9aaaa8170e08b76`.

Canonical evidence uses two official server JVMs and three client sessions.
Headless protocol-14 only. No GUI. No Aero.
