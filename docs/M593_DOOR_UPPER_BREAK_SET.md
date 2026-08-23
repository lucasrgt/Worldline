# M593-DOOR-UPPER-BREAK-SET door upper break set

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Breaking the upper half of a closed wooden door removes both door cells and drops wooden-door item 324. This is distinct from wooden-door open/toggle, remaining hinge/face orientation, and iron-door power.

## Qualification cycle

DataDrivenCycle rebuilds the raised stone plus wooden-door fixture in two fresh official server JVMs. Each run places wooden door item 324 as BlockDoor 64:0/8, Packet14-breaks the upper half with iron axe 258, requires both cells air plus Packet21 324, and reloads those air cells after save plus fresh login. One official EOF is retried after a 5 second sleep. Headless B173WireClient protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,lower=4:72:4:64:0->0:0,upper=4:73:4:64:8->0:0,drops=packet21-324,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `ff0b9e220d130cc2f0c5f67eabccc0b161c409c17334c027717bda1cab2aa07a`.
