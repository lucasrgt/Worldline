# M327 qualification cycle

`FoodCraftsCycle` rebuilds one official food-crafts SET in two fresh
official server JVMs. Each run crafts sugar in the personal 2x2 grid and
stew `282`, bread `297`, cookies `357`, and cake `354` on a workbench,
then reloads those stacks after save plus a fresh login. One official EOF
is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`feb202ff5d2172def94a39a6a9e560b5e4ecdba79681b018a8e046bb89703a54`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero. This cycle does not eat food.

Run directly with:

```text
java tools/smoke/FoodCraftsCycle.java m327-food-crafts
```
