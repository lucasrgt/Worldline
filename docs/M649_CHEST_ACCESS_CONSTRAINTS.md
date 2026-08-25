# M649-CHEST-ACCESS-CONSTRAINTS chest access constraints

<!-- Generated from smoke.properties by MilestoneNarrative. -->

## Claim

Official Beta 1.7.3 exposes two independent chest access constraints. An uncovered single chest opens Packet100 title Chest with 27 owned slots and a 63-slot view, while a solid stone block immediately above a second chest suppresses Packet100 throughout a bounded thirty-tick observation. Two adjacent chests open one Large chest window with 54 owned slots and a 90-slot view, but a Packet15 attempt to place a third chest directly beside that pair is rejected: the target remains air and the final chest stack is not consumed. A save plus fresh reader retains the four chests, solid lid, rejected air cell, and both valid window shapes. This does not claim obstruction by transparent blocks, chest orientation, inventory transfer, or chest breaking.

## Qualification cycle

DataDrivenCycle rebuilds two fresh official dedicated-server replicas. A builder raises an eight-support stone row, opens an uncovered control chest, and constructs a lid above the blocked chest by stacking on the adjacent pillar and placing east, never right-clicking the chest. Empty-hand activation then proves bounded Packet100 absence for thirty ticks. The builder forms and opens a valid double chest, attempts a third placement against its empty support, observes air plus the retained chest stack, and reopens the unchanged 54-slot pair. After save and clean disconnect, a fresh reader re-observes the topology and valid single and double windows through ChestAccessFixture. Headless protocol-14 only. No GUI. No Aero.

Expected signal: `column=17,support=4:71:4:1:0,control=4:72:4:54:0,single=title=Chest,owned=27,total=63,lid=7:73:4:1:0,blocked=7:72:4:54:0,open=absent-30,left=10:72:4:54:0,right=11:72:4:54:0,double=title=Large chest,owned=54,total=90,third=9:72:4:0:0,rejected=true,held=54:1,persisted=true,clients=2,disconnect=clean`.

Frozen semantic SHA-256: `ff9cc5913f7b646063f89b51ae8400b6e85c6596ce2bd0f786e70e4b625eed1a`.
