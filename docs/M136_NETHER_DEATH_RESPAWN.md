# M136 Nether death respawn

Status: GO in Worldline v1.124.0.

M136 extends the typed respawn boundary across dimensions. An empty player
starts below the Nether void threshold, first proves decoded skyless
netherrack terrain, and reaches a signed nonpositive Packet8 health state.
The client sends Packet9 with byte `-1`; the official server responds with a
fresh Packet9 dimension `0` and restores health `20`.

`RemoteRespawn` now records both source and destination dimensions while its
original same-dimension constructor remains available. The real dimension
change clears the bounded Nether cache. After the server's pose correction,
every retained chunk is an Overworld chunk with positive skylight, and clean
save persists dimension `0`, health `20` and an empty inventory.

M136 does not claim cross-dimension inventory drops, bed spawn selection,
portal death, exact spawn coordinates, death messages, score/experience,
hardcore behavior, arbitrary mod dimensions or repeated death loops.
