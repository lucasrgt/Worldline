# M608-FARMLAND-DRY-SET farmland dry set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M608 qualifies official unhydrated farmland reversion as one SET. Wooden hoe Packet15 only tills isolated dirt 3 into farmland 60:0 under a rain roof with no nearby water. The actor stays off that cell. Official random ticks then write dirt 3. The frozen signal names dry=60->3. That dirt cell survives a clean save plus fresh login. This is distinct from M156 hydration, M304 hoe-till plus trample, M354 dry-versus-hydrated moisture, and M576 jump-fall trample. It does not claim wheat, hoe durability, rain hydration, other hoe materials, or a Worldline soil simulation. Headless B173WireClient protocol-14 only. No GUI. No Aero. Exact wait length is not hashed.

## Qualification cycle

DataDrivenCycle rebuilds the raised dry farmland cell in two fresh official server JVMs. Each run Packet15-hoes one isolated dirt plot 3 to farmland 60:0 under a rain roof, then waits a bounded random-tick window until Packet53 60->3 appears. One official EOF is retried after a 5 second sleep. The frozen signal must name dry=60->3 and must not claim hydration or trample oracles. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,cell=4:72:5:3:0,cover=4:74:5:1:0,hoe=290,farmland=60:0,dry=60->3,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `26798de451d0b61504e1945d3196c64186f4c482b60481d9d1b06a61303e1531`.
