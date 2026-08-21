# M432 qualification cycle

`RemainingRailGeometrySetCycle` rebuilds a raised stone column in two
fresh official server JVMs. Each run Packet15-places rail item `66` as an
east slope `66:2` and a south-east curve `66:6`, then reloads those cells
after save plus fresh login. The frozen signal must name slope `66:2` and
curve `66:6`. It must not claim powered rail `27` or detector rail `28`.
One official EOF is retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/RemainingRailGeometrySetCycle.java m432-remaining-rail-geometry-set
```

The frozen semantic SHA-256 is
`3da03f5b4d6dd509fa5fc0925d5ea7422d5cd6ddb96e7acb84b5854de2ab61b1`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
