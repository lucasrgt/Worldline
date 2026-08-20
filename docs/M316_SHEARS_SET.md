# M316 shears set

M316 opens the official compound shears harvest boundary. Packet14 while
holding shears item `359` breaks player-placed oak leaves `18:8` and drops
Packet21 stack `18`. Packet7 interact on a living type-`91` sheep drops
Packet21 wool item `35` without Packet38 status 3. The same sheep still
emits a movement packet.

Frozen semantic SHA-256:
`91eec7f3061f3c9cb956cd25ebcc7ece6a66055262a45538081a8ad72d79426e`.

This milestone clones M269 leaf shears and M329 living-sheep wool. It does
not claim dye colors, shears durability, cobweb harvest, death drops, or
breeding. Headless `B173WireClient` only. No GUI. No Aero.
