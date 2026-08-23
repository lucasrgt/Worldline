# M576-FARMLAND-TRAMPLE-SET farmland trample set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M576 qualifies official farmland trampling as one SET. Wooden hoe Packet15 only tills dirt 3 into farmland 60:0 as fixture setup. The actor then jumps and falls onto that cell until official onEntityWalking writes dirt 3. The frozen signal names trample=60->3. That dirt cell survives a clean save plus fresh login. This is distinct from M156 hydration, M304 hoe-till plus trample, and M354 dry-versus-hydrated moisture. It does not claim wheat, hoe durability, rain, other hoe materials, or a Worldline soil simulation. Headless B173WireClient protocol-14 only. No GUI. No Aero.

## Qualification cycle

DataDrivenCycle rebuilds the raised farmland cell in two fresh official server JVMs. Each run Packet15-hoes one dirt plot 3 to farmland 60:0, then Packet13 jump-fall until the official server writes dirt 3. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,cell=4:72:4:3:0,hoe=290,farmland=60:0,trample=60->3,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `460485cad949455638ecc3bf33174cb4a4e28e8d8a0ef7c1f26a829cfdfe72ba`.
