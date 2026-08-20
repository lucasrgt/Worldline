# M331 throwables set

M331 opens the official compound air-use throwable boundary. It clones the
M161 snowball, M169 egg, and M180 fishing-rod fixtures into one isolated
cycle. Packet15 direction `255` while holding snowball `332`, egg `344`,
and fishing rod `346` emits Packet23 types `61`, `62`, and `90` on the
existing object tracker. Two headless peers observe identical identity,
type, and thrower for each spawn.

Frozen semantic SHA-256:
`63d18b0a65f745ad18fa9a7a9e8e345e8bffe83e067224ff8687c1b03c0a7328`.

This milestone does not claim snowball collision, egg hatch, fishing catch
RNG, inventory decrement hashing, Packet28 velocity, or arrows. Headless
`B173WireClient` only. No GUI. No Aero.
