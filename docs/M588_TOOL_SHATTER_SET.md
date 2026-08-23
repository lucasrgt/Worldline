# M588-TOOL-SHATTER-SET tool shatter set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Using a wooden pickaxe with 1 durability remaining on cobble destroys the held tool. The hotbar slot becomes empty and stays empty after a clean save plus fresh login. This is distinct from M352 remaining durability damage of 1.

## Qualification cycle

DataDrivenCycle rebuilds the raised stone plus cobble fixture in two fresh official server JVMs. Each run Packet14-breaks cobble 4 with wooden pickaxe 270 seeded at damage 59, then reloads the empty held slot after save plus fresh login. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,cobble=4:72:4:4:0->0:0,wood=270:59->empty,control=270:0,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `f1fe9cfe500117ca6ec28bf02fd1afd06a79fd20a1415660e79bbdcb77346a54`.
