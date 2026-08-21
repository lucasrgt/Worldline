# M35 Bounded Movement Outcome

Two fresh protocol-14 clients first request a collision-safe `+0.125 X` move.
The bounded session must classify it as `UNCHALLENGED`. Each client then moves
into a nearby solid block selected from its decoded cache; an actual inbound
Packet13 must classify that attempt as `CORRECTED` and restore the last
unchallenged pose.

After the rollback, the original cached chunk must remain loaded. Each client
disconnects cleanly, the official server saves, and persisted player NBT must
equal the small unchallenged pose. This persistence check is what qualifies
that bounded unchallenged result as server-accepted evidence.

Frozen expected signature SHA-256: `414c83fa237a0affd1c36ab171e04f07ab110487fc2ebd75698f54e55d92417a`
