# M462 bow mob hit set

M462 opens the official player-arrow-hit SET. Seeded bow `261` plus arrows
`262` air-use through Packet15 so Packet23 type `60` hits Packet24 pig
`90` and records Packet38 status `2`. The same session family then
retargets the spawner to `Zombie` at night `14000` so type `54` receives
the same hurt. The frozen signal names bow `261`, type `60`, pig `90`,
zombie `54`, and Packet38 status `2`.

This family is distinct from M436 land-then-collect, M157 two-peer type-60
identity, and M332 workbench crafts plus shoot-only type `60`. It does not
claim skeleton-shot arrows, stuck-arrow pickup, death, or drop loot.

The frozen semantic SHA-256 is
`bbe6e87049578c8e26c8cca6f79ed7ac1f3c530df498b2d9da63a8f195578e22`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
