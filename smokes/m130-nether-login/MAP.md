# M130 behavior map

The server profile opts into the official Nether. Before first login,
`B173PlayerSeed.writeDimension` writes an empty player with NBT `Dimension=-1`.
The official server accepts that state, sends normal login/pose/chunk traffic,
and saves the player back in dimension `-1`.

The first decoded chunk contains exact netherrack, bedrock and lava counts plus
an ordered structural terrain hash. Mushroom decorations 39/40 are normalized
to air because their count varied with fresh generation order.

Frozen semantic SHA-256:
`ec56849776288464b6b19f00d5e977802847f155bcd1d8139a3816c7c53b7824`.
