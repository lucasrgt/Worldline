# M395 qualification cycle

`RemainingDyeMixCycle` rebuilds the remaining-dye fixture in two fresh
official server JVMs. Each run mixes cyan, pink, and light blue dye in the
personal 2x2 grid and reloads those stacks. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`1ba82fec7effc4370c0a4169136f177851484511a3b86bf1d2aaf76134e1491c`.

Run directly with:

```text
java tools/smoke/RemainingDyeMixCycle.java m395-remaining-dye-mix
```

Canonical evidence uses two official server JVMs and four client sessions.
