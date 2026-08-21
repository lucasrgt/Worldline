# M332 bow arrow set

M332 opens the official compound bow-and-arrow SET. Sticks `280` plus string
`287` in a placed workbench `58` produce bow `261`. Flint `318`, one stick,
and feather `288` produce arrows `262` (stack of four). The actor then
air-uses the crafted bow through Packet15 direction `255`. The existing
Packet23 tracker observes object type `60` whose thrower is the shooter
entity id.

The frozen semantic SHA-256 is
`b745e8656e459e93ffe617759990be48c4c454450256e53f8ef1c5bf1757d215`.

This milestone does not claim hit damage, stuck-arrow pickup, inventory
decrement hashing, snowballs, eggs, fishing floats, TNT or falling-sand
object types, durability, or a second Packet23 tracker. Headless
`B173WireClient` protocol-14 only. No GUI. No Aero.
