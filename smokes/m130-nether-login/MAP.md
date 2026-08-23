<!-- worldline-map-schema=1 -->
<!-- boundary=m130-nether-login -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=d04ef062cdda13bb2209d8f6651f0559495d9a9f63f946f460b0e8610c41c4a8 -->

# M130 behavior map

The server profile opts into the official Nether. Before first login,
`B173PlayerSeed.writeDimension` writes an empty player with NBT `Dimension=-1`.
The official server accepts that state, sends normal login/pose/chunk traffic,
and saves the player back in dimension `-1`.

The first decoded chunk contains exact netherrack and bedrock counts plus an
ordered structural terrain hash. Lava 10/11 and mushroom decorations 39/40 are
normalized to air because scheduled flow and decoration varied before capture.

Frozen semantic SHA-256:
`d04ef062cdda13bb2209d8f6651f0559495d9a9f63f946f460b0e8610c41c4a8`.
