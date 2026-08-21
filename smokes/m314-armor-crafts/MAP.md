# M314 behavior map

Packet15 places workbench item `58` on a raised stone column. Packet102 then
crafts iron helmet `306`, chestplate `307`, leggings `308`, and boots `309`
from iron ingots `265` in the opened 3x3 workbench. Those four result ids
remain in personal storage after a clean save plus fresh login. Armor slots
5-8 stay empty, so this map is not M270-M273 equipment.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+workbench58+ingot265|cause=packet102-workbench-iron-armor|wire=packet103-result-306-307-308-309|oracle=live-craft+fresh-login+unequipped|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,crafts=306,307,308,309,ingredient=265,equipped=false,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`914b42df18b53c2afcbb40f2f5c87b8848dc19e4e816eaef927067915c98b437`.
