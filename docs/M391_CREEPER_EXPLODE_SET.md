# M391 creeper explode set

M391 opens the official compound creeper-explosion SET. A saved mob
spawner is retargeted from `Pig` to `Creeper`. After midnight the
headless protocol-14 client observes Packet24 type `50`, stays inside
the vanilla proximity fuse, and records Packet60 at strength `3`. One
blast lists base-layer dirt `3` and wool `35` among multiple destroyed
cells, and a fresh login retains a nonempty crater. Exact ray-selected
coordinates are deliberately not frozen.

This is distinct from M137 TNT strength `4` and from M359 Nether-bed
strength `5`. The frozen signal names type `50`, strength `3`, and the
wool-plus-dirt crater.

Frozen semantic SHA-256:
`14ad8cdcf99568672d696cd1c79210ab82f31f2bb6bbda7f005f4c162d76f60c`.

This milestone does not claim charged creepers, gunpowder, exact blast
rays, or player lethal damage. Headless `B173WireClient` protocol-14
only. No GUI. No Aero.
