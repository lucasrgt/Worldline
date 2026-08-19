# M116 redstone wire power

Status: GO in Worldline v1.104.0.

M116 extends the neutral block-interaction surface with
`useHeldItemOnBlock(support, face)`. Unlike `placeHeldBlock`, this action accepts
the full positive protocol item-ID range, so item 331 can execute its official
on-block behavior and create redstone wire 55. The adapter still derives the
item, count and damage from the server-authoritative selected inventory slot
and requires a synchronized personal window with empty cursor.

The fixture reuses M115's deterministic ten-stone column. Redstone dust is
used on its top face, producing wire `55:0`; a side lever remains `69:1` after
200 stabilization ticks. Empty-hand activation then produces lever `69:9`
and wire `55:15` after ten signal ticks. Both transitions arrive through
Packet53 and persist in a fresh session's Packet51.

Exactly those two states differ across the complete chunk. The ordered delta
SHA-256 is
`5f8ada70879cd4ae2c504a2bafb3664d468caa4a7f2c7e4caaf119347c7d65b9`.

M116 proves one adjacent lever-to-wire power result. It does not claim generic
network topology, attenuation, depowering, update order, repeaters, torches,
doors, pressure plates, pistons, quasi-connectivity, circuits, timing,
cross-chunk propagation, rendering or arbitrary item-on-block behavior.
