# M547 qualification cycle

`StickyPistonQcSetCycle` rebuilds the cloned sticky-`29` QC arm in two
fresh official server JVMs. Each run powers the block ABOVE sticky
piston `29`, extends through quasi-connectivity, pulls the payload on
retract, and reloads the final arm after save plus fresh login. The
frozen signal includes `qc-extend` and `qc-pull`. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`21af5dafa50bb529a1c0264a2be27d9b92aa0728c007fae07ecbef1547d92b1d`.

Run directly with:

```text
java tools/smoke/StickyPistonQcSetCycle.java m547-sticky-piston-qc-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
