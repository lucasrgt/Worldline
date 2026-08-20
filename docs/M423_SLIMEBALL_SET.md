# M423 slimeball set

M423 opens the official size-1 slimeball plus sticky-piston craft boundary.
A headless protocol-14 client places one default spawner in seed
`17320110707` slime chunk `-2,-2` below `y=16`, rewrites MobSpawner
`EntityId` to `Slime`, observes Packet24 type `55`, and kills until a
size-1 death emits Packet21 slimeball `341`. The same session opens
workbench `58` and Packet102-crafts sticky piston `29` from piston `33`
plus slimeball `341`. That drop-plus-craft family is the SET, distinct from
M412 parent-split whose slimeball drop stayed outside the frozen hash, and
from M371's seeded machine-block crafts.

Drop count stays outside the frozen hash; drop presence of item `341` is
inside it.

Frozen semantic SHA-256:
`8a525200f72521e3f129de58b27232e197c39cf2d41da689d2772e9a830ac411`.

This milestone does not claim XP, breeding, despawn, loot-table counts,
sticky pull, or other hostile types. Headless `B173WireClient` only. No GUI.
No Aero.
