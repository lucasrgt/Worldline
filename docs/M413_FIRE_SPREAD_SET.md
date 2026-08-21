# M413 fire spread set

M413 opens the official compound fire-spread boundary. Packet15 of
flint-and-steel item `259` on netherrack `87` places fire `51` in the air
cell above. Official scheduled fire ticks then place fire `51` on the air
cells above adjacent planks `5`, leaves `18`, and wool `35` in the same
cycle. Packet53 fire `51` on those three spread cells is latched because a
later snapshot may already show consume-to-air. The frozen signal names
the source fire cell, the three spread fire cells, and `spread-steps=3`.

This family is distinct from M343 netherrack persist plus wool consume,
from M151 netherrack persist, and from M152 wool consumption. It does not
claim ice or snow melt, rain extinguishing, fire charge, or Nether-dimension
ignition.

Beta 1.7.3 has no `doDaylightCycle` gamerule. Exact wait length is not
hashed.

Frozen semantic SHA-256:
`e8fdef86a6fe2bd49b4575a296bc67cfe62dce1f2eb89aefd7ca2e89aa70843c`.

Headless `B173WireClient` only. No GUI. No Aero.
