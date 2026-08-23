# M591-FURNACE-SMELT-INTERRUPT-SET furnace smelt interrupt set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Removing furnace input or fuel mid-progress prevents a completed cobble-to-stone output that would otherwise finish. One idle furnace 61:2 completes cobble 4 plus coal 263 to stone 1. A second furnace loads the same recipe, waits until burning 62, then Packet102 takes the input; slot 2 stays empty past cook 200. A third furnace loads coal, waits 40 ticks, Packet102 takes that fuel, then loads cobble without fuel; slot 2 stays empty. Distinct from M60 complete smelt, M221 idle placement, M296 recipe outputs, and M338 fuel burn durations.

## Qualification cycle

DataDrivenCycle rebuilds the raised stone fixture in two fresh official server JVMs. Each run places three idle furnaces 61:2, completes one cobble smelt as the otherwise-finish control, then interrupts input mid-cook and fuel before consume. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,control=4:72:4:61:2,input=5:72:4:61:2,fuel=3:72:4:61:2,recipe=4->1,interrupt=input+fuel,mid=40,wait=220,control-out=1x1:0,input-out=empty,fuel-out=empty,clients=1,disconnect=clean`.

Frozen semantic SHA-256: `b201db62647312f9c38b74691f478bc5177a508b9e0894c9dfd00069df7cb689`.
