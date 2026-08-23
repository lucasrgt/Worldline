# M615-REDSTONE-ONE-TICK-SET redstone one tick set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

M615 opens the official 1-tick redstone pulse boundary. Packet15 turns east wall lever 69:1 to 69:9, then Packet15 cuts it immediately so the pulse is one Packet53 on-then-off. Sticky piston 29 retracts without pulling, leaving cobble in the pushed cell. Distinct from M555 torch burnout, M341 delay cycling, M142 held extension, and retracted M557's 4-tick hold that never observed 69:9.

## Qualification cycle

DataDrivenCycle rebuilds the west sticky 29 and east wall lever in two fresh official server JVMs. Each run cuts the lever on Packet53 69:9, latches dropped cobble, and reloads those cells after save plus fresh login. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=10,pulse=one-tick,drop=sticky-payload,piston=4:65:4:29:4,head=3:65:4:4:0->0:0,pushed=2:65:4:0:0->4:0,lever=5:64:4:69:1->9->1,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `21d4ac5bbdf54331350d6fd27043f140c2b7d3930a4466bb8c191d0c7fd93a8a`.
