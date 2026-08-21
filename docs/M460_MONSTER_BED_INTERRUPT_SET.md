# M460 monster bed interrupt set

M460 qualifies the official Beta 1.7.3 dedicated-server bed sleep plus
monster interrupt family as one compound SET. Item `355` Packet15 against a
raised grass platform places block `26` as foot metadata `0` and head
metadata `8` for yaw `0`. A MobSpawner `52` is retargeted from `Pig` to
`Zombie`. Console `time set 14000` is the night gate. Empty-hand Packet15
then occupies the head (`26:8` becomes `26:12`) and emits Packet17Sleep at
the head cell. The Packet70Bed tracker records no game-state reason (`-1`).

With `spawn-monsters=true`, Packet24 type `54` appears near the bed. The
official server wakes the sleeping actor on hostile attack, clearing the
occupied bit (`26:12` back to `26:8`) while world time stays night. That
leave is the SET interrupt, distinct from M330 morning skip.

This family does not claim M330 occupy/wake without monsters, M359 Nether
bed explode, M431 remaining bed facings, or rain Packet70.

Frozen semantic SHA-256:
`252160a06c2d628ac1441c16105e90c2c1e0047f10a300061765a01948d87c61`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
