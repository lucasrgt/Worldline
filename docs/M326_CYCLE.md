# M326 qualification cycle

`VehicleCraftsCycle` rebuilds the raised workbench fixture in two fresh
official server JVMs. Each run crafts boat `333`, minecart `328`, chest
minecart `342`, and furnace minecart `343` from their vanilla recipes and
reloads those stacks after save plus fresh login. One official EOF is
retried after a 5 second sleep.

The frozen semantic SHA-256 is
`1109c4ce19cf7f23d5156d80cef725329fc62a68c438e24d4294aa468e088bdc`.

Run directly with:

```text
java tools/smoke/VehicleCraftsCycle.java m326-vehicle-crafts
```

Canonical evidence uses two official server JVMs and four client sessions.
