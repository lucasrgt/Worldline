# M635-NATURAL-SLIME-SPAWN natural slime spawn

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M635 freezes natural Beta 1.7.3 slime spawning inside a formula-selected 7x7 chunk matrix below height sixteen. The fixed matrix contains sixteen independently verified slime chunks. The fixture controls subterranean geometry only: it solidifies the matrix below y=60 and carves three vertically separated 14x14x4 rooms entirely below y=16 in each qualifying chunk. It never places or rewrites a mob spawner. After a clean restart with monsters enabled, the official loop must emit a type-55 Packet24 in one of those exact formula-selected chunks. The claim excludes swamp spawning, arbitrary seeds, spawn frequency, exact coordinates, exact slime size, and slime combat.

## Qualification cycle

DataDrivenCycle runs two fresh official dedicated-server replicas at seed 17320110707 with view distance three. Each replica generates and saves all 49 loaded chunks with monsters disabled, controls their underground block geometry, then restarts with monsters enabled and a central surface observer more than 24 blocks from every room. NaturalSlimeSpawnFixture owns a 4,800-attempt bound, revalidates the successful chunk formula, and normalizes entity ID, coordinate, tick, and size variation. No spawner block, spawner NBT, mob insertion, or runtime RNG write is used.

Expected signal: `seed=17320110707,matrix=95:101:40:46,slime-chunks=16,geometry=matrix-solid-below60+3xrooms14x14x4-under16,type=55,y<16,formula=verified,natural=no-spawner,bounded<=4800,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `3ec318921ce8e424ea5a897c436ffc45d1e69fb09a3ce71808ba3c425db3ecc2`.
