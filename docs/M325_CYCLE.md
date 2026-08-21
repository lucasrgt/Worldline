# M325 qualification cycle

`NavigationCraftsCycle` rebuilds the workbench fixture in two fresh official
server JVMs. Each run crafts compass `345`, clock `347`, and empty map `358`
from their vanilla recipes and reloads those stacks after save plus fresh
login. One official EOF is retried after a 5 second sleep.

The frozen semantic SHA-256 is
`904591e822865303c647ea818403edb8d115b37da19262cb96387da6f2e4302d`.

Run directly with:

```text
java tools/smoke/NavigationCraftsCycle.java m325-navigation-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
Headless protocol-14 only. No GUI. No Aero.
