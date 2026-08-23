# M391 creeper explode set

M391 opens the official compound creeper-explosion SET. A saved mob
spawner is retargeted from `Pig` to `Creeper`. After midnight the
headless protocol-14 client observes Packet24 type `50`, stays inside
the vanilla proximity fuse, and records Packet60 at strength `3`. One
blast removes both materials from a `7×7` checkerboard of dirt `3` and wool
`35`; the exact Packet60-selected cells remain air after fresh login.

This is distinct from M137 TNT strength `4` and from M359 Nether-bed
strength `5`. The frozen signal names type `50`, strength `3`, and the
wool-plus-dirt crater.

Frozen semantic SHA-256:
`389f99f5639c66342a8560c23fe7e85cbe1aafc6e71530ed05c0cc7bbdbb19c0`.

This milestone does not claim charged creepers, gunpowder, exact blast
rays, or player lethal damage. Headless `B173WireClient` protocol-14
only. No GUI. No Aero.
