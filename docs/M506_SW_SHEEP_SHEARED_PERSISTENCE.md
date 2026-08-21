# M506-SW sheep sheared-state persistence

M506-SW replaces the factually invalid grass-regrowth proposal with a boundary
that exists in the official Beta 1.7.3 JAR. `EntitySheep` stores color and the
`Sheared` bit in data-watcher index `16`, and writes both `Color` and `Sheared`
to NBT.

The cycle proves red `14` becomes sheared red `30`, survives a server restart,
and cannot drop wool again while sheared. A white `0` sheep is the negative
control. An offline smoke-only mutation of exactly one persisted `Sheared`
boolean restores metadata `14`; a subsequent official shearing returns it to
`30` and emits a new red-wool Packet21.

M506-SW does not claim grass grazing, wool regrowth, breeding, natural spawn,
or a general entity-NBT editing API.

Frozen semantic SHA-256:
`57aca1de84ec46a162610d18a48ba190b6128e62cc628d70e9e6ef92361790bd`.
