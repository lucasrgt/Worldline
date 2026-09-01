# M751-IRON-DOOR-HAND-REJECTION iron door hand rejection

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M751 freezes that the official Beta 1.7.3 dedicated server rejects manual toggling of a placed closed iron door. The actor raises a stone column over water, places official iron door item 330 as BlockDoor 71 halves 71:0 and 71:8, then sends empty-hand Packet15 activation at the lower half and again at the upper half. After each attempt the settled server world still reports exactly 71:0 and 71:8 with no block change, and a clean save plus fresh-login reader persists both closed halves unchanged. This is the zero-edit rejection contrast to M277 wooden-door toggling and M629 door sound events; it does not claim powered iron-door behavior, lever or redstone activation, M118 circuit semantics, door sounds, or open-state metadata.

## Qualification cycle

DataDrivenCycle rebuilds the raised fixed-seed closed-iron-door fixture in two fresh official dedicated-server JVMs. Each replica pre-seeds the actor with stone and one iron door item, builds the raised column, places both door halves, activates the lower half by empty hand then the upper half by empty hand, settles the server ticks after each attempt, requires both halves to remain exactly 71:0 and 71:8, saves cleanly, and reconnects a fresh reader that observes both persisted halves still closed. IronDoorHandFixture normalizes coordinates while retaining exact state semantics as equatable evidence. Headless B173WireClient only. No GUI. No Aero.

Expected signal: `door=71:0/8,lower-hand=rejected,upper-hand=rejected,preserved=71:0/8,persisted=closed,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `e8f4f08fbbf53ebc5be10da0fb4931c55779f21c37f2c92b7704bb5487294c9c`.
