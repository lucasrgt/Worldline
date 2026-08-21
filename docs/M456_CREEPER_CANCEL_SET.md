# M456 creeper cancel set

M456 opens the official creeper proximity-fuse cancel SET. A saved mob
spawner is retargeted from `Pig` to `Creeper`. After midnight the headless
protocol-14 client observes Packet24 type `50` on the raised pad, records
Packet40 DataWatcher index `16` = `1` as the vanilla 3-block fuse starts,
then Packet13-steps west (cap 9) beyond the 7-block continue range. Index
`16` returns to `-1`, and Packet60 is absent after a bounded 45-tick wait.
The pad and spawner stay intact.

This is distinct from M391 creeper Packet60 strength `3` cratering, from
M448 fuse-then-explode if the actor stays, and from M421 gunpowder `289`.

Frozen semantic SHA-256:
`b0006bb940528fa914ae436cfe7b3ae4b73e26a997596d9275fb9c851da2e1fc`.

This milestone does not claim charged creepers, gunpowder, exact fuse-tick
counts, or player lethal damage. Headless `B173WireClient` protocol-14
only. No GUI. No Aero.
