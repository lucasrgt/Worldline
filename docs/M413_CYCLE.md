# M413 qualification cycle

`FireSpreadSetCycle` rebuilds the raised netherrack-plus-plank-leaf-wool
fixture in two fresh official server JVMs. Each run uses flint-and-steel
item `259` to place fire `51` on netherrack `87`, then waits a bounded
scheduled-fire-tick window until fire `51` appears on adjacent planks `5`,
leaves `18`, and wool `35`. One official EOF is retried after a 5 second
sleep.

The frozen signal must name multiple fire cells and `spread-steps=3`.

Run directly with:

```text
java tools/smoke/FireSpreadSetCycle.java m413-fire-spread-set
```

The frozen semantic SHA-256 is
`e8fdef86a6fe2bd49b4575a296bc67cfe62dce1f2eb89aefd7ca2e89aa70843c`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
