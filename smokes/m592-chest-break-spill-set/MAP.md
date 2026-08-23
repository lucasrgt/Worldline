<!-- worldline-map-schema=1 -->
<!-- boundary=m592-chest-break-spill-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=61d5e21ae388e4eae74bbff8642019d5e46c6cf82e538dc89c58f7d71562685b -->

# M592 chest break spill behavior map

The fixture raises isolated stone and places one official chest `54:0`.
Packet102 stores cobble `4x1` in slot 0 and dirt `3x1` in slot 1 of the
single `Chest` window (27 owned slots). Packet14 with gold axe `286`
then breaks the chest to air.

`BlockChest` removal spills each stored stack as Packet21, and the chest
block itself drops Packet21 item `54`. Those three entity IDs are
distinct. The cell stays air after a clean save plus fresh login.

This map does not claim chest place metadata persistence (M232) or
remaining chest orient (M433). Headless `B173WireClient` protocol-14
only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+chest54+cobble4+dirt3|cause=packet15-item54+packet102-store-4+packet102-store-3+packet14-goldaxe286|wire=packet100-Chest-27+packet21-4x1+packet21-3x1+packet21-54|oracle=chest-break-spill-not-place-not-orient|column=17,support=4:71:4:1:0,chest=4:72:4:54:0->0:0,load=4x1+3x1,spill=packet21-4x1+packet21-3x1,chest-drop=packet21-54,persisted=air,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`61d5e21ae388e4eae74bbff8642019d5e46c6cf82e538dc89c58f7d71562685b`.
