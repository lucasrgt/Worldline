# M556 qualification cycle

`RsNorLatchSetCycle` rebuilds the two-torch RS-NOR latch in two fresh
official server JVMs. Each run places north torch `76:4` and south torch
`76:3`, waits for RESET `75:4` plus `76:3`, arms the hold path, enables SET,
then disables it and requires the pair to stay ON. It enables RESET, disables
it, requires the pair to stay OFF, and reloads that RESET pair after save plus
a fresh login. The signal
names both complementary torch cells and is distinct from M312's single
`76:4 -> 75:4` invert and from M555 burnout. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`7241b7297eea8617a084daaf981b2001119180794ec82ab3fbd7d664a55537ad`.

Run directly with:

```text
java tools/smoke/RsNorLatchSetCycle.java m556-rs-nor-latch-set
```

Canonical evidence uses two official server JVMs and five client sessions per
server JVM (ten sessions total).
Headless protocol-14 only. No GUI. No Aero.
