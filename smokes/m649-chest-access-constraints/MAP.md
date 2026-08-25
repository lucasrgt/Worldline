<!-- worldline-map-schema=1 -->
<!-- boundary=chest-access-constraints -->
<!-- nonclaims=transparent-obstruction,orientation,inventory-transfer,chest-breaking -->
<!-- frozen-trace=ff9cc5913f7b646063f89b51ae8400b6e85c6596ce2bd0f786e70e4b625eed1a -->

# M649 chest access constraints behavior map

The official Beta 1.7.3 server builds four chests on a raised eight-support stone row. The
uncovered control opens Packet100 `Chest` with 27 owned slots and a 63-slot view. A second chest
has solid stone `1:0` immediately above it; the lid is placed east from a two-block stack on the
adjacent pillar, so fixture construction never activates the chest. Empty-hand activation of the
blocked chest emits no Packet100 throughout a telemetered thirty-tick window.

The final two chests form an already-qualified `Large chest` with 54 owned slots and a 90-slot
view. Packet15 then targets the empty support directly west of that pair. The official server
rejects the third chest: the target stays air `0:0`, the last chest stack remains `54:1`, and the
unchanged pair reopens at 54/90. A fresh reader observes the same four chest cells, solid lid,
rejected air cell, and valid single and double windows after save.

`worldline.testkit.ChestAccessFixture#verify` normalizes unstable window IDs and compares only
topology, window shapes, bounded lid blocking, and third-placement rejection as equatable
evidence. This boundary does not claim transparent-block obstruction, orientation, inventory
transfer, or chest-breaking behavior.

Frozen signal:

```text
column=17,support=4:71:4:1:0,control=4:72:4:54:0,single=title=Chest,owned=27,total=63,lid=7:73:4:1:0,blocked=7:72:4:54:0,open=absent-30,left=10:72:4:54:0,right=11:72:4:54:0,double=title=Large chest,owned=54,total=90,third=9:72:4:0:0,rejected=true,held=54:1,persisted=true,clients=2,disconnect=clean
```

Frozen trace SHA-256: `ff9cc5913f7b646063f89b51ae8400b6e85c6596ce2bd0f786e70e4b625eed1a`.
