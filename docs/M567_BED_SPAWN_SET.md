# M567 bed spawn set

M567 qualifies the official Beta 1.7.3 dedicated-server bed spawn-point
family as one compound SET. Item `355` Packet15 against a raised 3x3 stone
pad places block `26` as two halves: foot metadata `0` and head metadata `8`
for yaw `0`. Console `time set 18000` is the existing lab night gate.
Empty-hand Packet15 then occupies the head (`26:8` becomes `26:12`) and
emits Packet17Sleep at the head cell. The Packet70Bed tracker records no
game-state reason (`-1`) for this Overworld occupy.

With `spawn-monsters=false`, one player reaches `sleepTimer >= 100` and the
official server skips to morning, clearing the occupied bit (`26:12` back to
`26:8`) and writing player `SpawnX/Y/Z` from the bed head. The actor is
standing again. Sand `12` plus cactus `81` is then placed on the east pad.
Cactus contact drives Packet8 health to `0`. Packet9 returns the actor to
the bed, not `level.dat` `SpawnX/Y/Z`.

This milestone is distinct from M330 occupy/wake without death, from M135
wait-under-kill respawn at world spawn, and from M469 void death without a
bed. It does not claim Nether bed explosions, rain Packet70, occupied chat,
or client bed rendering.

Frozen semantic SHA-256:
`aaad061b562df911b0b4c29784fe2beb4b0d5f1183dae8e29603cd3c2a838aed`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
