# M330 bed sleep set

M330 qualifies the official Beta 1.7.3 dedicated-server bed sleep family as
one compound SET. Item `355` Packet15 against a raised stone pair places
block `26` as two halves: foot metadata `0` and head metadata `8` for yaw
`0`. Console `time set 18000` is the existing lab night gate. Empty-hand
Packet15 then occupies the head (`26:8` becomes `26:12`) and emits
Packet17Sleep at the head cell. The Packet70Bed tracker records no
game-state reason (`-1`) for this Overworld occupy.

With `spawn-monsters=false`, one player reaches `sleepTimer >= 100` and the
official server skips to morning, clearing the occupied bit (`26:12` back to
`26:8`). The actor is standing again. That leave, or the same standing pose
after a clean save plus fresh login, is required beside the enter. The frozen
signal includes both `enter=26:8->26:12` and `leave=26:12->26:8` plus
`persisted=wake`.

This milestone is distinct from M158 daytime refusal and from M240
place-only. It does not claim spawn-point persistence after death, Nether bed
explosions, rain Packet70, occupied chat, or client bed rendering.

Frozen semantic SHA-256:
`1415f89a64178b9c0135d108239ba04eb9fca293f9d8ee9005347624eb6842af`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
