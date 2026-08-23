# M572-DETECTOR-RAIL-VACATE-SET detector rail vacate set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M572 opens the official detector-rail occupy-then-vacate boundary. A sloped detector rail 28 powers while a minecart occupies it, then unpowers after the cart rolls onto a lower landing rail. This is distinct from unpowered detector place (M185) and occupied-and-persisted detector geometry (M402).

## Qualification cycle

DataDrivenCycle rebuilds the sloped detector and landing rail in two fresh official server JVMs. Each run places minecart 328 on detector 28, observes powered occupancy, waits until the cart leaves and the rail unpowers, then reloads the vacated cells after save plus fresh login. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,high=4:72:3:1:0,detector=4:72:4:28:4->12->4,landing=4:72:5:66:0,cart=type10+thrower0+fixed144:2331:144,occupy=28:12,vacate=28:4,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `fddc5e78e37e693e02add3085bf1e0c53f9d464a11c3784f5b7814b28274f5ba`.
