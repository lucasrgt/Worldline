# M421 creeper gunpowder set

M421 opens the official creeper-gunpowder family. One saved mob spawner is
retargeted from `Pig` to `Creeper`. After midnight (`time set 14000`) the
headless protocol-14 client observes Packet24 type `50`, kills it with
Packet7 diamond sword `276` from outside the proximity fuse, and collects
Packet21 gunpowder `289`. The same session Packet102-crafts TNT `46` from
gunpowder `289` plus sand `12` on workbench `58`. TNT `46` stays in
inventory and is not placed.

This is distinct from M391 creeper Packet60 strength `3` and from M417 TNT
place plus flint-and-steel Packet60 chain. It is also distinct from M371's
machine-block crafts, which seed gunpowder without a creeper death.

Frozen semantic SHA-256:
`f01c7a65ddde0ddb0cd8f27f6e1c76e896f866c0bf9cc6f8af973bd1def648dc`.

This milestone does not claim charged creepers, explosion rays, TNT fuse,
XP, loot-table counts, or other hostile types. Headless `B173WireClient`
only. No GUI. No Aero.
