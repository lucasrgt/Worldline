# M626-DUNGEON-GENERATION-CENSUS dungeon generation census

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M626 freezes fixed-seed Beta 1.7.3 dungeon generation across a populated 11x11 chunk matrix. The public TestKit fixture records every spawner 52 and spawner-linked chest 54, selects one accessible chest, and requires a nonempty authoritative Packet100 loot window after a clean save/restart.

## Qualification cycle

DataDrivenCycle executes two fresh official dedicated-server replicas. Each replica generates the same 121 chunks, compares the equatable spawner/chest census, restarts the saved world beside a selected linked chest, and hashes its nonempty loot slots. The claim is bounded to this seed and region; arbitrary seeds, mob-spawner EntityId, and loot probabilities remain nonclaims.

Expected signal: `region=-5:5:-5:5,chunks=121,dungeon=spawner+linked-chest,loot=nonempty-packet100,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `546390d97d39a29b825727f8264038033d7e8144f284200a1a8819069ebd78a8`.
