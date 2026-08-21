# M454 peaceful despawn set

M454 opens the official Peaceful versus Easy hostile-despawn family.
Dedicated-server `difficulty=0` (Peaceful) keeps Packet24 types among
`50`, `51`, and `54` absent or despawned. `difficulty=1` (Easy) with
`spawn-monsters=true` lets Creeper type `50` and Zombie type `54`
persist after a spawner rewrite. Both profiles share one frozen SET.

This is distinct from M435 natural spawn identity without a difficulty
contrast. It does not claim loot, combat, spider type `52`, M363
zombie-plus-skeleton identity, or M390 creeper-plus-spider identity.
Headless `B173WireClient` protocol-14 only. No GUI. No Aero.

The frozen semantic SHA-256 is
`8a4c4acadf23008e8fed2fdbc1d9c05c903c65c527c3489dabee48e7d2183abe`.
