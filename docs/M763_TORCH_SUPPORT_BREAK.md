# M763-TORCH-SUPPORT-BREAK torch support break

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M763 opens the official wall-torch support-break lifecycle boundary. Packet15 of torch item 50 against the east face of a raised stone support writes wall torch 50:1 in the adjacent cell; Packet14 with iron pickaxe 257 breaking that support pops the torch to air 0:0 and drops exactly one torch item as Packet21, and the popped cell persists as air after save plus fresh login. This is distinct from floor-torch placement metadata (M175), remaining wall-torch faces (M400), redstone burnout (M555), and water wash-off of a floor torch (M599).

## Qualification cycle

DataDrivenCycle rebuilds the raised stone column in two fresh official server JVMs. Each run raises the cloned column to the deterministic support cell, places wall torch 50 on the east face and observes 50:1, Packet14-breaks the support with iron pickaxe 257, observes the torch cell pop to air plus the exact Packet21 torch 50x1 drop, then reloads the chunk after clean save plus fresh login and requires the popped cell persisted as air. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0->0:0,torch=5:71:4:50:1->0:0,drop=packet21-50x1,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `be5e59f43b9242f0a2828026130188ee1d0ed53ca5a69604da1eb6630919598e`.
