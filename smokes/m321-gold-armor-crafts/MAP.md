<!-- worldline-map-schema=1 -->
<!-- boundary=m321-gold-armor-crafts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a44c48c91eba492305c1faa7963dd3ad1023d9a9a97bd6ccd92c2b8abcec9fbf -->

# M321 behavior map

Packet15 places workbench item `58` on a raised stone column. Packet102 then
crafts gold helmet `314`, chestplate `315`, leggings `316`, and boots `317`
from gold ingots `266` in the opened 3x3 workbench. Those four result ids
remain in personal storage after a clean save plus fresh login. Armor slots
5-8 stay empty, so this map is not M271 gold-chestplate equipment and is not
the M314 iron-armor crafts.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+workbench58+ingot266|cause=packet102-workbench-gold-armor|wire=packet103-result-314-315-316-317|oracle=live-craft+fresh-login+unequipped|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,crafts=314,315,316,317,ingredient=266,equipped=false,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a44c48c91eba492305c1faa7963dd3ad1023d9a9a97bd6ccd92c2b8abcec9fbf`.
