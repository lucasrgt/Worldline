# M345 qualification cycle

`OreBlockCraftsCycle` rebuilds the raised workbench fixture in two fresh
official server JVMs. Each run crafts gold block `41`, iron block `42`,
diamond block `57`, and lapis block `22` from nine ingots, gems, or dyes
and reloads those stacks after save plus fresh login. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`1a72ee9100a460729b226ac6ea350567f9a953cb2d8832d43545f74bdf9f0427`.

Run directly with:

```text
java tools/smoke/OreBlockCraftsCycle.java m345-ore-block-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
