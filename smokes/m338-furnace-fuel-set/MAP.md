# M338 furnace fuel set map

Packet15 places three idle furnaces `61:2` on a raised stone fixture. Each
furnace opens with Packet100 type 2 / `Furnace` and Packet104 size 39.
Packet102 actions 1-4 load cobble `4x1:0` plus one fuel. Packet103/105
complete cook 199 and the fuel's official burn duration, then publish stone
`1x1:0` in slot 2.

Fuels in one cycle, same cobble input:

- coal `263x1:0` → Packet105 burn 1600, completion 1401
- oak planks `5x1:0` → Packet105 burn 300, completion 101
- lava bucket `327x1:0` → Packet105 burn 20000, completion 19801

This map is distinct from M296 iron/gold/pork outputs. It does not claim
charcoal `263:1` (same item 263 and the same 1600-tick burn as coal),
output pickup, XP, or burning furnace `62` as a placement product.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+3xfurnace61:2+cobble4+coal263+planks5+lava327|cause=packet15-item61+packet102-load-4+263+5+327|wire=packet100-type2-Furnace-39+packet103-output1-slot2+packet105-cook199-burn1600+300+20000|oracle=idle-61:2+live-stone1-fuels-263+5+327|column=17,support=4:71:4:1:0,coal=4:72:4:61:2,planks=5:72:4:61:2,lava=3:72:4:61:2,input=4->1,fuels=263+5+327,coal=1600:1401,planks=300:101,lava=20000:19801,out=1x1:0,cook=199,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`d412ed91eacea33e26daaf3f37c6494ecb462ee19694093f7126187f36a2b957`.
