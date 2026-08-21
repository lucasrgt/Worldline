# M141 behavior map

The fixture raises a `7×7` platform with forty-eight grass blocks around one
stone center. Placing mob spawner `52:0` creates the official default tile
entity, whose fixed entity name is `Pig` and whose first delay is twenty ticks.

The production decoder consumes Packet24 as entity ID, type byte, three fixed
point integers, yaw/pitch bytes and unique typed metadata entries terminated by
`127`. A byte fixture freezes a 25-byte pig payload with two metadata entries.
Two connected clients then require one identical official type-`90` packet,
positive non-player identity, flags zero and spawn pose inside the spawner's
bounded random volume.

Frozen semantic SHA-256:
`a148241c4e0282a64cf461ef362991e001cc17b1c7b06bd12e3f7b5b555fd522`.
