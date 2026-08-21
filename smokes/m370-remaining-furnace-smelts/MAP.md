# M370 remaining furnace smelts map

Packet15 places three idle furnaces `61:2` on a raised stone fixture. Each
furnace opens with Packet100 type 2 / `Furnace` and Packet104 size 39.
Smoke-local Packet102 actions 1-4 load one remaining recipe plus coal `263x1:0`.
Packet103/105 complete cook 199, burn 1600, total 1600, reset 0, completion
burn 1401.

Recipes in one cycle, distinct from M296 iron/gold/pork and M324 sand/cobble/fish:

- cactus `81x1:0` → cactus green `351x1:2`
- oak log `17x1:0` → charcoal `263x1:1`
- clay ball `337x1:0` → brick `336x1:0`

Public furnace-load identities still reject cactus, log, and clay, so this map
keeps Packet102 clicks smoke-local. It does not claim pickup, XP, alternate
fuels, or burning-furnace placement as a product.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+3xfurnace61:2+cactus81+log17+clay337+coal263|cause=packet15-item61+packet102-load-81+17+337|wire=packet100-type2-Furnace-39+packet103-output351:2+263:1+336-slot2+packet105-cook199|oracle=idle-61:2+live-cactusgreen351:2+charcoal263:1+brick336|column=17,support=4:71:4:1:0,furnaces=3x61:2,cactus=81->351:2,log=17->263:1,clay=337->336,cook=199,burn=1600,completion=1401,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`912452d315840ced68811ccce77f3cde4f1250eac7068c5ddd9f85e22a607a2a`.
