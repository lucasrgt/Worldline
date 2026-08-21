# M319 qualification cycle

`StairSlabCraftsCycle` rebuilds the workbench fixture in two fresh official
server JVMs. Each run crafts oak stairs `53`, cobble stairs `67`, and stone
slab `44` from their vanilla recipes and reloads those stacks after save
plus fresh login. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`cec4e38d37d31058c744ff1e9c806d2567fcf878603f2e63cdf7347058f5d553`.

Run directly with:

```text
java tools/smoke/StairSlabCraftsCycle.java m319-stair-slab-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
