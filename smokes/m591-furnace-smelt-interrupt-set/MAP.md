<!-- worldline-map-schema=1 -->
<!-- boundary=m591-furnace-smelt-interrupt-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b201db62647312f9c38b74691f478bc5177a508b9e0894c9dfd00069df7cb689 -->

# M591 furnace smelt interrupt set behavior map

Packet15 places three idle furnaces `61:2` on a raised stone fixture. Each
furnace opens with Packet100 type 2 / `Furnace` and Packet104 size 39.

The control furnace Packet102-loads cobble `4x1:0` plus coal `263x1:0` and
completes Packet103 stone `1x1:0` in slot 2 with Packet105 cook 199 / burn
1600. That is the otherwise-finish recipe.

The input furnace loads the same recipe, waits until the world cell is
burning `62`, then Packet102 takes slot 0 back to personal inventory. After
220 more ticks slot 2 stays empty.

The fuel furnace Packet102-stores coal in slot 1, waits 40 ticks while idle
`61:2`, Packet102-takes that fuel, then stores cobble without fuel. After
220 ticks slot 2 stays empty.

This map is distinct from M60's completed sand smelt, M221 idle furnace
placement, M296 iron/gold/pork outputs, and M338 coal/planks/lava burn
durations. It does not claim charcoal, output pickup, XP, or burning
furnace `62` as a placement product.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+3xfurnace61:2+cobble4+coal263|cause=packet15-item61+packet102-load-4+263+take-input-or-fuel|wire=packet100-type2-Furnace-39+packet103-slot2-empty+packet105-cook-reset|oracle=idle-61:2+interrupt-no-stone-not-m60-m221-m296-m338|column=17,support=4:71:4:1:0,control=4:72:4:61:2,input=5:72:4:61:2,fuel=3:72:4:61:2,recipe=4->1,interrupt=input+fuel,mid=40,wait=220,control-out=1x1:0,input-out=empty,fuel-out=empty,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`b201db62647312f9c38b74691f478bc5177a508b9e0894c9dfd00069df7cb689`.
