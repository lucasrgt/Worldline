# M372 qualification cycle

`PlaceableItemCraftsCycle` rebuilds one official placeable-item-crafts SET
in two fresh official server JVMs. Each run crafts sticks in the personal
2x2 grid and painting `321`, sign `323`, and bowls `281` on a workbench,
then reloads those stacks after save plus a fresh login. One official EOF
is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`80d1b7a10efe73807810ca2609135b07e47ba57880f35e72d1b205e27394a993`.

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero. This cycle does not place
signs, write text, orient paintings, or eat food.

Run directly with:

```text
java tools/smoke/PlaceableItemCraftsCycle.java m372-placeable-item-crafts
```
