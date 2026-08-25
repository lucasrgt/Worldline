# M702-POWERED-RAIL-SLOPE-PROPAGATION powered rail slope propagation

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M702 opens the official powered-rail power-propagation slope boundary. A redstone torch at the base of a sloped powered rail 27 writes the powered bit on the slope itself and propagates it uphill across the slope boundary to the higher flat rail and downhill to two lower flat rails; breaking the torch restores every rail to its idle shape. This is distinct from flat-line rail power states (M309), powered acceleration (M377), unpowered braking (M595), and slope geometry placement only (M432).

## Qualification cycle

DataDrivenCycle rebuilds the raised north-south slope run in two fresh official server JVMs. Each run places powered rails 27 as low, far, top-of-slope-support, then the sloped cell, verifies every idle shape, places torch item 76 east of the slope base, observes bit 8 on all four rails across the boundary, holds the powered state, breaks the torch, observes full unpower restoration, then reloads every cell after save plus fresh login. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,high=4:72:3:1:0,slope=4:72:4:27:4->12->4,top=4:73:3:27:0->8->0,low=4:72:5:27:0->8->0,far=4:72:6:27:0->8->0,torch=5:72:4:76:5->0,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `fce5958ba41931d5f80c5281e83f1f4c1939b0ec88ee47577d057f2d5fc59e91`.
