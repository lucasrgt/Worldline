# M445 skeleton ranged AI set

M445 opens the official skeleton archery family. One saved mob spawner is
retargeted from `Pig` to `Skeleton`. After `time set 14000` the headless
protocol-14 client observes Packet24 type `51` and then two Packet23 type
`60` arrows whose `throwerId` is that skeleton entity. Both arrows share
one frozen SET. A 24-block fence perimeter keeps the selected spawn inside
the platform, and full diamond armor keeps the observer alive without changing
the archery oracle. Drop counts, loot-table arrows, and hit damage stay
outside the frozen hash.

This is distinct from player bow `261` (M157/M332/M436) and from skeleton
bone `352` (M422). It does not claim arrow item `262`, XP, or other
hostile types. Headless `B173WireClient` only. No GUI. No Aero.

The frozen semantic SHA-256 is
`c397640bf9dddee3c3b93081c4816f82f93289ba759f499d2865fad69fb5d888`.
