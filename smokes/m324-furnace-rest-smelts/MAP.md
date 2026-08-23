<!-- worldline-map-schema=1 -->
<!-- boundary=m324-furnace-rest-smelts -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=6c131f48c758cb5376dfd0b9504b154148f17e3295c1337c08e4c32619dc781a -->

# M324 furnace rest smelts map

Packet15 places three idle furnaces `61:2` on a raised stone fixture. Each
furnace opens with Packet100 type 2 / `Furnace` and Packet104 size 39.
Packet102 actions 1-4 load one remaining recipe plus coal `263x1:0`.
Packet103/105 complete cook 199, burn 1600, total 1600, reset 0, completion
burn 1401.

Recipes in one cycle, distinct from M296 iron/gold/pork:

- sand `12x1:0` → glass `20x1:0`
- cobblestone `4x1:0` → stone `1x1:0`
- raw fish `349x1:0` → cooked fish `350x1:0`

This map does not claim pickup, XP, charcoal, clay brick, cactus green, or
burning-furnace placement as a product.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+3xfurnace61:2+sand12+cobble4+fish349+coal263|cause=packet15-item61+packet102-load-12+4+349|wire=packet100-type2-Furnace-39+packet103-output20+1+350-slot2+packet105-cook199|oracle=idle-61:2+live-sand20+cobble1+fish350|column=17,support=4:71:4:1:0,furnaces=3x61:2,sand=12->20,cobble=4->1,fish=349->350,cook=199,burn=1600,completion=1401,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`6c131f48c758cb5376dfd0b9504b154148f17e3295c1337c08e4c32619dc781a`.
