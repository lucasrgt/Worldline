# M133 Nether portal traversal

Status: GO in Worldline v1.121.0.

M133 turns the typed dimension boundary into a real server-authored lifecycle.
The client builds and activates the M132 frame, enters its portal blocks and
sustains the official residence interval. Packet9 changes the live session from
dimension `0` to `-1`; subsequent pose and chunk packets populate only the new
remote world.

The destination chunk has Nether skylight and terrain, plus the official server
generated counterpart portal. Fresh worlds agree on its chunk and structural
counts but not its exact block coordinate, so the latter is dynamic evidence
rather than a frozen claim. A clean save retains player dimension `-1`.

M133 does not claim exact portal-search coordinates, return travel, scaling for
arbitrary coordinates, portal reuse, cooldown semantics beyond the qualified
single traversal, entity transport, simultaneous travelers, death/respawn, or
client rendering.
