# M419 remaining netherrack place

M419 qualifies the official Beta 1.7.3 dedicated-server Nether terrain
family as one compound SET. A dimension `-1` player seed logs in through
the M130 Nether profile and Packet15-places netherrack `87`, soul sand
`88`, and glowstone `89` on a natural netherrack platform.

The frozen signal names dimension `-1` and blocks `87`, `88`, and `89`.
All three cells survive a clean save plus fresh login.

This family is distinct from shipping M224 (one Overworld netherrack
cell), M192 (one Overworld soul-sand cell), M191 (one Overworld
glowstone cell), M357 (glowstone-dust crafts of `89`), M343
fire-on-netherrack, and M382 portal lighting. Headless `B173WireClient`
protocol-14 only. No GUI. No Aero.

Frozen semantic SHA-256:
`c7dec53dcc70e1baa573a851f8e296853cfe16d36ddd182d1cfd5e83a8a4dea7`.
