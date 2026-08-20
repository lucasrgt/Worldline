# M328 qualification cycle

`DyeFamilyCraftsCycle` rebuilds the bone, flower, and ink fixture in two
fresh official server JVMs. Each run crafts bone meal, rose red, dandelion
yellow, and gray dye in the personal 2x2 grid and reloads those stacks. One
official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`7ae29bcd82b147e1286ec7a3b4655087822ac5f5379f18142eab3fd163dda815`.

Run directly with:

```text
java tools/smoke/DyeFamilyCraftsCycle.java m328-dye-family-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
