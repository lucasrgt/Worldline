# M391 creeper explode set

M391 opens the official compound creeper-explosion SET. A saved mob
spawner is retargeted from `Pig` to `Creeper`. After midnight the
headless protocol-14 client observes Packet24 type `50`, stays inside
the vanilla proximity fuse, and records Packet60 at strength `3`. One
blast removes nearby dirt `3` and wool `35` as multiple destroyed cells.

This is distinct from M137 TNT strength `4` and from M359 Nether-bed
strength `5`. The frozen signal names type `50`, strength `3`, and the
wool-plus-dirt crater.

Frozen semantic SHA-256:
`2a74b9f63925b31966343a26c78c5b6d87dcdb84096822099fe3988f5d59b771`.

This milestone does not claim charged creepers, gunpowder, exact blast
rays, or player lethal damage. Headless `B173WireClient` protocol-14
only. No GUI. No Aero.
