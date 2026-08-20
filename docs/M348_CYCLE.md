# M348 qualification cycle

`DyeMixCraftsCycle` rebuilds the primary-dye fixture in two fresh official
server JVMs. Each run mixes orange, purple, and lime dye in the personal
2x2 grid and reloads those stacks. One official EOF is retried after a 5
second sleep.

The frozen semantic SHA-256 is
`2c8b97b5aa9c68fef810b33465f38d10146adbfcea7c9994c7742c0ae1305b94`.

Run directly with:

```text
java tools/smoke/DyeMixCraftsCycle.java m348-dye-mix-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
