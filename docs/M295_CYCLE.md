# M295 qualification cycle

`PressurePlatesCycle` rebuilds the raised-stone fixture in two fresh official
server JVMs. Each run places wooden plate `72` and stone plate `70`, powers
both by standing on the cells, depowers both by stepping off, and reloads
unpowered metadata after save plus login. One official EOF is retried after
a 5 second sleep.

The frozen semantic SHA-256 is
`d36cbe38c632dcc4e03334db2982db51f92e78b36850dcbc098b43469ebb9815`.

Run directly with:

```text
java tools/smoke/PressurePlatesCycle.java m295-pressure-plates
```

Canonical evidence uses two official server JVMs and four client sessions.
