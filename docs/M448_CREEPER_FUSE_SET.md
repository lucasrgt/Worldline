# M448 creeper fuse set

M448 opens the official compound creeper-fuse SET. A saved mob spawner
is retargeted from `Pig` to `Creeper`. After midnight the headless
protocol-14 client observes Packet24 type `50`, stays inside the vanilla
proximity fuse, and records Packet40 ignited state `1` before Packet60.
The frozen signal names the ordered family fuse-then-explode. Exact
fuse tick length is not hashed.

This is distinct from M391 Packet60 strength `3` wool-plus-dirt crater
hashing, from M421 gunpowder `289`, and from M456 fuse-cancel-on-leave.

Frozen semantic SHA-256:
`702d4dc074d1db9a965d74f49f1318cb05a4397c343a59b8fde15a3ab8f15505`.

This milestone does not claim charged creepers, crater rays, gunpowder,
or leaving the fuse radius. Headless `B173WireClient` protocol-14 only.
No GUI. No Aero.
