# M436 remaining arrow life set

M436 opens the official remaining bow-and-arrow life SET. Seeded bow `261`
plus arrows `262` air-use through Packet15 so Packet23 type `60` lands with
the actor as thrower. Packet14 then drops the remaining arrow stack as
Packet21 item `262`. After the official pickup delay, Packet103 restores
arrow `262` to the hotbar. The frozen signal names bow `261`, arrow `262`,
type `60`, pickup, and collect.

This family is distinct from M332 workbench crafts plus shoot-only type
`60`, and from M157 two-peer type-`60` identity. It does not claim stuck
in-ground `onCollideWithPlayer` pickup of the Packet23 object, hit damage,
durability, snowballs, eggs, or fishing floats.

The frozen semantic SHA-256 is
`9a370fd980f9abd2ed3f852ff575a9dae9c9b0f461c73fa548d131b40077011c`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
