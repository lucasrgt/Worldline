# M149 pig death

M149 advances the living-entity boundary from creation and movement to an
authoritative death. `MobObservationSession` now exposes `attackMob` and
immutable `RemoteMobDeath` evidence for protocol-14 Packet38 status 3 paired
with Packet29 destroy.

The official server runs the opt-in animal-enabled profile. A default pig
spawner creates a shared Packet24 identity on the raised grass platform. The
actor selects an undamaged diamond sword and sends Packet7 against that
identity. Both peers independently decode the same death: hurt status 2, death
status 3 and entity destroy.

Random porkchop count remains a vanilla `nextInt(3)` outcome. This milestone
does not claim drop identity, drop count, knockback, animation, despawn of
unrelated animals, hostile AI, breeding, saddle state or persistence of dead
mobs across restart.
