# M548 qualification cycle

`PistonBudSetCycle` rebuilds the cloned piston-`33` BUD arm in two fresh
official server JVMs. Each run places a neighbor torch on the payload,
observes one official extension pulse that self-clears to retracted
`33:4`, and reloads that final arm after save plus fresh login. The frozen
signal includes `bud-pulse` and `power=none`. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`64edc418a23140583ce5015dead697010582f99862cc5e19d6e8e7e53f02bcff`.

Run directly with:

```text
java tools/smoke/PistonBudSetCycle.java m548-piston-bud-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
