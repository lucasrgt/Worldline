# M581-ITEM-STACK-MERGE-SET item stack merge

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Two identical Packet21 stone stacks dropped in contact remain two live entities. Official EntityItem does not absorb one stack into the other, so Packet29 destroy and a remaining count increase stay absent. This is distinct from M517 EntityItem age-6000 despawn.

## Qualification cycle

DataDrivenCycle rebuilds the two-stone drop in two fresh official server JVMs. Each run Q-drops two 1x1 stone entities while looking down, waits a 30-tick contact window inside the pickup-delay, and requires live=2 with merged=false. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `drops=2,item=1x1:0,live=2,destroyed=0,collected=0,merged=false,contact=30,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `e4e993fb7359eaf26b59c61d904faefeef4a3fa3e5193f800d5a8538015a22fc`.
