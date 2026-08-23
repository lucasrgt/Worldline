# M611-DOUBLE-CHEST-MERGE-SET double chest merge

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Two adjacent chests form a large chest with a shared 54-slot Packet100 window. A lone chest first opens as title Chest with 27 owned slots; placing one adjacent chest merges that window to title Large chest with 54 owned slots. This is distinct from M232 single-chest place and orientation, which never opens Packet100.

## Qualification cycle

DataDrivenCycle rebuilds the raised two-block stone pad in two fresh official server JVMs. Each run places one chest, opens Packet100 title Chest with 27 owned slots, places an adjacent chest, then opens Packet100 title Large chest with 54 owned slots. Fresh login keeps the merged 54-slot window. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,east=5:71:4:1:0,left=4:72:4:54:0,right=5:72:4:54:0,single=title=Chest,owned=27,total=63,merged=title=Large chest,owned=54,total=90,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `638adc227adc9c23b9840e4745a2596cd5dde899442a3510271da150d03980c9`.
