# M549 qualification cycle

`StickyBudSetCycle` rebuilds the cloned sticky-`29` BUD arm in two fresh
official server JVMs. Each run places sticky piston `29` with a
diagonal-above lever-powered block so the arm stays primed `29:4`,
neighbor-updates it to BUD-extend, then pulses the lever off for an
official sticky pull, and reloads the final arm after save plus fresh
login. The frozen signal includes `primed`, `bud-extend`, `sticky-pull`,
and `continuous-power=false`. One official EOF is retried after a 5
second sleep.

The frozen semantic SHA-256 is
`d9de32a7e37b272dd97be1d211464f0bf67b7b66ba71ddedbf3742d0f345747b`.

Run directly with:

```text
java tools/smoke/StickyBudSetCycle.java m549-sticky-bud-set
```

Canonical evidence uses two official server JVMs and four client
sessions. Headless protocol-14 only. No GUI. No Aero.
