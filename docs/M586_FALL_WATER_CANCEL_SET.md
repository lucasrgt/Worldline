# M586-FALL-WATER-CANCEL-SET fall water cancel set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

A fall that would damage on solid ground deals no fall Packet8 when the landing is still water. A headless B173WireClient walks off a raised stone column into an east still-water 9 pool from a drop of at least six blocks. Vanilla water cancels accumulated fallDistance before the grounded landing analog, so health stays 20 and Packet8 stays absent. Health 20 survives a clean save plus fresh login. This is distinct from M461 taller-hurts-more solid pads, M307 drowning, and M469 void death.

## Qualification cycle

DataDrivenCycle rebuilds the raised-stone plus east still-water pool in two fresh official server JVMs. Each run walks off a damaging-height drop into still water 9, freezes Packet8 absence at health 20, and confirms the same health after save plus fresh login. One official EOF is retried after a 5 second sleep. The frozen signal must name water=20->20 and packet8=absent. It must not collapse to M461 taller=true solid-pad damage, M307 drowning, or M469 void death. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=18,support=4:72:4:1:0,pool=5:65:4:9:0,drop=8,water=20->20,packet8=absent,status=0,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `86e26ad940cc22ea7b653cdcde5be882da3a7d531b60f1da99787f6d9a4bd7a2`.
