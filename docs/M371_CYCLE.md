# M371 qualification cycle

`MachineBlockCraftsCycle` rebuilds the raised workbench fixture in two fresh
official server JVMs. Each run crafts TNT `46`, piston `33`, and sticky
piston `29` from their vanilla recipes and reloads those stacks after save
plus fresh login. One official EOF is retried after a 5 second sleep.
Headless `B173WireClient` is the only client. There is no GUI and no Aero
path.

Run directly with:

```text
java tools/smoke/MachineBlockCraftsCycle.java m371-machine-block-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`51d13daf2febf456a423e84d136707a77b9117668bc3979f4b52514bdbb26c7e`.
