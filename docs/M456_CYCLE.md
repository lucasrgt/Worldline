# M456 qualification cycle

`CreeperCancelSetCycle` rebuilds a raised grass pad in two fresh official
server JVMs. Each run retargets one spawner `EntityId` to `Creeper`, sets
night, observes Packet24 type `50`, waits until Packet40 index `16` becomes
`1`, then steps west (cap `9` per Packet13) until index `16` returns `-1`.
Packet60 must stay absent after a 45-tick wait. One official EOF is retried
after a 5 second sleep. Headless `B173WireClient` is the only client. There
is no GUI and no Aero path.

Run directly with:

```text
java tools/smoke/CreeperCancelSetCycle.java m456-creeper-cancel-set
```

Canonical evidence uses two official server JVMs and four client sessions.
The frozen semantic SHA-256 is
`b0006bb940528fa914ae436cfe7b3ae4b73e26a997596d9275fb9c851da2e1fc`.
