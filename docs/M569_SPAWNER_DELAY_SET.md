# M569 spawner delay set

M569 opens the official mob-spawner delay and activation-range SET. One
saved mob spawner `52` is retargeted from `Pig` to `Zombie` and its NBT
`Delay` is rewritten to `1`. After midnight (`time set 14000`) a headless
protocol-14 client 24 blocks above the spawner must not observe Packet24
type `54` for a bounded 40-tick wait. Stationing inside the 16-block
activation range must then publish Packet24 type `54`.

This is distinct from M141's type-`90` pig identity, M390's creeper type
`50` plus spider type `52` identity, and M564 spawn light. Delay/range is
the claim, not living-type identity.

The frozen semantic SHA-256 is
`f8a3a81f8f2295adbcd12af662bb00620eea7f2d0b09701089362e062b5d0b19`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
