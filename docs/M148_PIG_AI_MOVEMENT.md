# M148 pig AI movement

M148 advances the M141 living-entity boundary from creation to authoritative
movement. `MobObservationSession` now exposes immutable `RemoteMobMovement`
evidence for protocol-14 Packet31, Packet33 and Packet34 position transitions.

The official server runs an opt-in `spawn-animals=true` profile while retaining
monsters disabled. A default pig spawner creates a shared Packet24 identity on
the raised grass platform. Both peers then independently decode the same first
nonzero horizontal transition for that entity, in 1/32-block fixed-point units.

Natural mobs that were not explicitly taken from the bounded spawn queue do not
accumulate movement evidence. This prevents unrelated animal traffic from
exhausting the tracker while retaining strict parsing and state updates.

Random direction and exact coordinates remain dynamic. This milestone does not
claim pathfinding goals, obstacle navigation, speed, timing, animation, damage,
despawn, breeding, hostile AI, generic entity movement or persistence of mobs.
