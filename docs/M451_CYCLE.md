# M451 qualification cycle

`ArmorReductionSetCycle` rebuilds the raised grass platform in two fresh
official server JVMs. Each run retargets one saved MobSpawner `EntityId`
to `Zombie`, seats leather `298-301` then iron `306-309` then diamond
`310-313` through window-0 Packet102 slots `5-8`, and records Packet8
health drops from the same type-`54` melee. Unarmored damage is strictly
greater than leather and iron. One official EOF is retried after a 5
second sleep. Headless `B173WireClient` is the only client. There is no
GUI and no Aero path.

Run directly with:

```text
java tools/smoke/ArmorReductionSetCycle.java m451-armor-reduction-set
```

Canonical evidence uses two official server JVMs and eight client
sessions. The frozen semantic SHA-256 is
`b04b51a3cb23c8254f44a5a8fddd04c0066bb3be81e60bd7b8ffde3ae89b0897`.
