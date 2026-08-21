# M346 qualification cycle

`OreBlockUncraftsCycle` rebuilds one official personal 2x2 ore-block uncraft
SET in two fresh official server JVMs. Each run seeds gold block `41`, iron
block `42`, diamond block `57`, and lapis block `22`, uncrafts them to nine
ingots or gems, and reloads the stored stacks after save plus a fresh login.
One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`6cb6facb7859e30e6d0834273f32ba84f01bede6c1d8d39ad7dcf6b33818f452`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero. This cycle does not place
blocks.

Run directly with:

```text
java tools/smoke/OreBlockUncraftsCycle.java m346-ore-block-uncrafts
```
