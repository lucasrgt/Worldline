# M747-SHEARS-LEAF-DURABILITY shears leaf durability

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M747 qualifies the Beta 1.7.3 shears leaf-harvest durability boundary as a focused reusable contract. Pristine held shears item 359 breaking exactly one placed oak-leaf block 18:8 removes the cell, emits one Packet21 oak-leaf stack 18:1:0, and moves the held stack from damage zero to exactly damage one through Packet103. This is distinct from the leaf-drop versus bare-hand contrast (M269), sheep shearing (M316, M506), and generic tool wear (M352, M587, M588); it excludes multi-break accumulation, login persistence, sapling chances, and other leaf species.

## Qualification cycle

DataDrivenCycle executes two fresh official dedicated-server replicas at seed 17320110707. Each replica raises a stone column clear of water, places oak log 17 on its east face and one oak-leaf block 18 above the log, verifies both live states, breaks only that leaf with held pristine shears 359 through Packet14, observes Packet53 air plus the Packet21 oak-leaf stack plus the Packet103 shears update, and ShearsLeafDurabilityFixture rejects wrong tool identity, wrong drop identity, non-air cells, and any durability transition other than exactly one point.

Expected signal: `tool=shears359,leaf=18:8->0:0,drop=packet21-18:1:0,shears=359:0->359:1,replicas=2,disconnect=clean`.

Frozen semantic SHA-256: `34f781cee4aaae6e7ef7792e8c26bc3c5e525aa5363d0b525bf36089eb25104a`.
