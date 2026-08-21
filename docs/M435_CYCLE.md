# M435 qualification cycle

`RemainingNaturalSpawnsCycle` rebuilds a dry stone column in two fresh
official server JVMs. Each run boots with `spawn-monsters=true`, sets
night, and requires at least two Packet24 types among `50`, `51`, `52`,
and `54` without a MobSpawner rewrite. One official EOF is retried after
a 5 second sleep.

Run directly with:

```text
java tools/smoke/RemainingNaturalSpawnsCycle.java m435-remaining-natural-spawns
```

The frozen semantic SHA-256 is
`a81852d5c2fb5cff300186da2b5d585a72f06f637dffb1942c4a8f1f2284d6d3`.

Canonical evidence uses two official server JVMs and two client sessions.
Headless protocol-14 only. No GUI. No Aero.
