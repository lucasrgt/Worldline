# M337 qualification cycle

`UtilityItemCraftsCycle` rebuilds the raised workbench fixture in two fresh
official server JVMs. Each run crafts shears `359` and flint-and-steel `259`
in the personal 2x2 grid, crafts empty bucket `325` on workbench `58`, and
reloads those stacks after save plus fresh login. One official EOF is retried
after a 5 second sleep.

The frozen semantic SHA-256 is
`24941b7cbf8eca87a6e5f03001a622de0dfb51a8d4e4f754906557bfa7603367`.

Run directly with:

```text
java tools/smoke/UtilityItemCraftsCycle.java m337-utility-item-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
