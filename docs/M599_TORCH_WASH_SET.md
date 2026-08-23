# M599-TORCH-WASH-SET torch wash set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Flowing or placed water occupying a floor-torch cell pops official torch 50 into a Packet21 item drop. This is distinct from M175 floor-torch placement metadata and M400 remaining wall-torch faces.

## Qualification cycle

DataDrivenCycle rebuilds the raised 3x3 stone fixture in two fresh official server JVMs. Each run places still water 9 beside floor torch 50:5, opens a dirt gate so flowing water occupies the torch cell, and requires Packet21 torch 50 plus a persisted water cell after fresh login. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,platform=3x3,support=4:71:4:1:0,torch=3:72:4:50:5->9:1,source=4:72:4:9:0,gate=5:72:4:3:0->0:0,drop=packet21-50x1,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `d262e072e7c810157ca1604a9cfa36fc4eb2e926588f44cee4bf41676177f38e`.
