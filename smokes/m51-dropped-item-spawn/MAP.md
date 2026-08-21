# M51 Dropped Item Spawn

The actor acquires one qualified stone while isolated. After an independent
observer confirms the carried item, the actor emits the M50 Packet14 status-4
drop action. The observer decodes the matching Packet21 entity ID, item stack,
fixed-point position, and signed-byte velocity into one immutable value.

The item spawn must be near the actor's last server-authoritative pose and have
non-zero bounded launch velocity. Packet103 and Packet5 still prove that the
actor and peer hand became empty, while clean player NBT remains empty.

This cycle does not claim item collection, destroy-entity correlation, exact
throw trajectory determinism, arbitrary entity tracking, container actions,
server tick control, or server-memory inspection.
