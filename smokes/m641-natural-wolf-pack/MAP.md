<!-- worldline-map-schema=1 -->
<!-- boundary=natural-wolf-pack -->
<!-- nonclaims=spawn-frequency,successful-rng-attempt,exact-entity-ids,exact-position,taming -->
<!-- frozen-trace=9d569a2c54537c423df37151039cf3eb192f860d07611a2f15bcff1737509d12 -->

# M641 semantic map

Evidence: official-server dual replica.

The public boundary is `worldline.testkit.NaturalWolfPackFixture#await`. It accepts two through
eight distinct type-95 observations whose pairwise horizontal distance is at most forty-eight
blocks. Its equatable evidence retains only that pack boundary and the sixty-four-attempt ceiling.

The mapped path calls `SpawnerAnimals.performSpawning(world, false, true)` once in each fresh
attempt world. The official path calls the corresponding `bp.a(dj, false, true)` directly against
the hash-verified server JAR. The fixture controls exposed grass geometry, forest-or-taiga biome
selection, global-spawn distance, and attempt seed only. It never places a spawner, edits spawn
lists, constructs a wolf, or inserts an entity.

Frozen signal: `official oracle: MATCH`.

Frozen semantic SHA-256: `9d569a2c54537c423df37151039cf3eb192f860d07611a2f15bcff1737509d12`.
