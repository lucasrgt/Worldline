# M610-ICE-MELT-LIGHT-SET Ice melt light set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M610 qualifies torch-adjacent ice melt as one SET. Packet15 places ice item 79 on a raised stone support and floor torch item 50:5 on the east neighbor so official block light plus random ticks melt ice 79:0 to still water 9:0. The leftover persists across a clean save plus fresh login. This family is distinct from M193 ice place without melt and from M386 ice-and-snow compound melt. It does not claim snow melt, silk-touch, slipperiness, or biome/global melt. Headless B173WireClient protocol-14 only. No GUI. No Aero. Exact melt delay is not hashed.

## Qualification cycle

DataDrivenCycle rebuilds the raised-stone ice-and-torch fixture in two fresh official server JVMs. Each run Packet15-places ice 79 beside floor torch 50:5, then waits a bounded official-tick window until the ice cell is still water 9:0. One official EOF is retried after a 5 second sleep. Canonical evidence uses two official server JVMs and four client sessions. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,east=5:71:4:1:0,ice=4:72:4:79:0->9:0,torch=5:72:4:50:5,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `63ded528e3f7faed4c46a7fcdc0d097771b808128f8d977e3034bf8b390230a0`.
