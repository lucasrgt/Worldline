# M158 bed

M158 qualifies the official Beta 1.7.3 dedicated-server bed contract. Item
`355` Packet15 against a raised stone pair places block `26` as two halves:
foot metadata `0` and head metadata `8` for yaw `0`. Empty-hand activation
during daytime emits Packet3 `You can only sleep at night` and leaves both
halves unoccupied.

After `time set 18000`, the same activation occupies the head (`26:12`) and
emits Packet17Sleep at the head cell. With `spawn-monsters=false`, one player
reaches `sleepTimer >= 100` and the official server skips to morning, clearing
the occupied bit. Packet70Bed reason `0` is the missing-bed respawn state, not
the night-time gate; this Overworld session never observes it.

Frozen semantic SHA-256:
`ab95c0893977d3774ddf9672b77063db206c52479e9645e917f6f0d42d49f2f0`.

The milestone does not claim Nether bed explosions, rain Packet70, occupied
chat, spawn-point selection after death, or client bed rendering.
