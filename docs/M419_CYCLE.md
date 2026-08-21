# M419 qualification cycle

`RemainingNetherrackPlaceCycle` rebuilds a Nether netherrack platform in
two fresh official server JVMs. Each run logs a dimension `-1` actor,
Packet15-places netherrack `87`, soul sand `88`, and glowstone `89`, and
reloads those cells after save plus fresh login. The frozen signal must
name dimension `-1` and at least `87` plus `88`. One official EOF is
retried after a 5 second sleep.

Run directly with:

```text
java tools/smoke/RemainingNetherrackPlaceCycle.java m419-remaining-netherrack-place
```

The frozen semantic SHA-256 is
`c7dec53dcc70e1baa573a851f8e296853cfe16d36ddd182d1cfd5e83a8a4dea7`.

Canonical evidence uses two official server JVMs and six client sessions.
Headless protocol-14 only. No GUI. No Aero.
