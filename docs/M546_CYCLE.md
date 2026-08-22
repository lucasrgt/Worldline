# M546 qualification cycle

`PistonQcSetCycle` rebuilds the cloned piston-`33` QC arm in two fresh
official server JVMs. Each run powers the block above the piston so
normal piston `33` QC-extends and then retracts after the above-block
is unpowered, and reloads the final arm after save plus fresh login.
The frozen signal includes `qc-extend`, `qc-retract`, and
`direct-power=false`. One official EOF is retried after a 5 second
sleep.

The frozen semantic SHA-256 is
`10f2cb5507e55026ceafd72d6ab2c74a3ab228aad1924965b9844d149117d989`.

Run directly with:

```text
java tools/smoke/PistonQcSetCycle.java m546-piston-qc-set
```

Canonical evidence uses two official server JVMs and four client
sessions. Headless protocol-14 only. No GUI. No Aero.
