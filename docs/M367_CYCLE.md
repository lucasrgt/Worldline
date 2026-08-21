# M367 qualification cycle

`PistonMotionSetCycle` rebuilds the cloned piston-`33` and sticky-`29`
arms in two fresh official server JVMs. Each run extends and retracts
normal piston `33`, then sticky-pulls with piston `29`, and reloads both
final arms after save plus fresh login. The frozen signal includes
`extend`, `retract`, and `sticky-pull`. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`eeb597ce51f18b3841a00e606375efae5dfb531672564e34670469f420f304a8`.

Run directly with:

```text
java tools/smoke/PistonMotionSetCycle.java m367-piston-motion-set
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
