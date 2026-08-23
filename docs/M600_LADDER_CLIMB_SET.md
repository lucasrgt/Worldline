# M600-LADDER-CLIMB-SET ladder climb set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

A player colliding with a ladder can climb or remain stationed on it. Protocol-14 Packet13 pose y increases or holds versus falling in air. This is distinct from M174 east-face placement, M361 client-physics climb, and M447 spider wall climb.

## Qualification cycle

DataDrivenCycle rebuilds the raised two-cell east ladder fixture in two fresh official server JVMs. Each run Packet15-places ladder item 65 as two 65:5 cells, Packet13-falls in adjacent air, then Packet13-stations while occupying the ladder. Pose y must hold or increase versus the air fall. Both cells survive a clean save plus fresh login. One official EOF is retried after a five-second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=18,support=4:71:4:1:0,upper=4:72:4:1:0,ladder=5:71:4:65:5+5:72:4:65:5,face=east,ticks=10,air-fall=true,ladder-hold=true,climbed=true,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `70add79ff9fcd22f9d99b2ade7b5e0033e213175af28ac85e8b18b07e24966e3`.
