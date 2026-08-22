# M568 item despawn set

M568 opens the official dropped-item despawn SET. Seeded cobble `4`
drops through Packet14 so Packet21 lives, then Packet29 removes that
item at official age `6000` ticks (five minutes) without a collector.
NBT may advance `Age` between save and load; the official dedicated
server still emits Packet29 as the oracle.

This family is distinct from M51 spawn-only Packet21, M52 Packet22
collection plus Packet29, and M436 remaining arrow life. It does not
claim pickup, ownership, container actions, or server-memory
inspection.

The frozen semantic SHA-256 is
`b90bfdf125255b880fd496ce52fa92b784d5e3879fbf448482c44004bd2574f2`.

Headless `B173WireClient` protocol-14 only. No GUI. No Aero.
