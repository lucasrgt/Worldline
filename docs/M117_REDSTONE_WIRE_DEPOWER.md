# M117 redstone wire depower

Status: GO in Worldline v1.105.0.

M117 composes the M115 activation and M116 held-item boundaries without adding
a new public API. The official server creates a side lever and adjacent wire,
then the first activation must establish the exact powered precondition
`69:9` and `55:15`. A second empty-hand Packet15 toggles the same lever off.

After ten bounded signal ticks, Packet53 exposes lever `69:1` and wire `55:0`.
A clean disconnect/save followed by a fresh login and Packet51 must reproduce
both depowered states. Across the complete chunk, exactly those two cells
change from the powered snapshot. The ordered delta SHA-256 is
`b033ed6f394141aa6a6eb797e19e7e82d6b8e81a655f198e38a8dfe16779ba6f`.

M117 proves one adjacent wire's recovery to zero after its lever source turns
off. It does not claim generic networks, distance attenuation, update order,
fan-out, loops, repeaters, torches, doors, pressure plates, pistons, powered
consumers, cross-chunk propagation, tick-exact latency or a Worldline redstone
simulator.
