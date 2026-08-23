# M592-CHEST-BREAK-SPILL-SET chest break spill set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Breaking a chest that contains cobble 4 and dirt 3 spills those stacks as Packet21 item entities, together with the chest item itself. This is distinct from chest place metadata persistence and remaining chest orient.

## Qualification cycle

DataDrivenCycle rebuilds the raised stone plus loaded chest 54 in two fresh official server JVMs. Each run stores cobble 4x1 and dirt 3x1, Packet14-breaks the chest with gold axe 286, and requires Packet21 spills plus air after save and fresh login. One official EOF is retried after a 5 second sleep.

Expected signal: `column=17,support=4:71:4:1:0,chest=4:72:4:54:0->0:0,load=4x1+3x1,spill=packet21-4x1+packet21-3x1,chest-drop=packet21-54,persisted=air,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `61d5e21ae388e4eae74bbff8642019d5e46c6cf82e538dc89c58f7d71562685b`.
