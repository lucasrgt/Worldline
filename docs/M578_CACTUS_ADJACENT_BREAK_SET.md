# M578-CACTUS-ADJACENT-BREAK-SET cactus adjacent break set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M578 opens the official cactus adjacent-solid pop. A raised sand cell receives cactus 81:0. Packet15 of stone against the cactus east face places a horizontally adjacent solid. Official canBlockStay then fails, the cactus cell becomes air, and Packet21 drops cactus item 81. Those air and neighbor cells remain after a clean save plus fresh login. This is distinct from M167 isolated cactus persistence, M275 cactus contact damage, and M384 cactus plus sugar-cane growth.

## Qualification cycle

DataDrivenCycle rebuilds the raised-sand cactus fixture in two fresh official server JVMs. Each run plants cactus 81 on sand 12, places stone 1 on the cactus east face, and requires Packet53 air plus Packet21 item 81. Fresh login keeps the cactus cell air beside neighbor stone 1. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,sand=4:72:4:12:0,cactus=4:73:4:81:0->0:0,neighbor=5:73:4:1:0,drops=packet21-81,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `437e319b299a6629b32f3d6d89b00e7ce02d3f26fdb1300eeace32cc656301ca`.
