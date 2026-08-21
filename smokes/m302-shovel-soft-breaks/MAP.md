# M302 behavior map

Packet15 places dirt `3`, sand `12`, gravel `13`, and clay `82` one at a
time on the same raised stone column. Packet14 while holding gold shovel
item `284` fully breaks each cell to air and emits a Packet21 drop.

The signal names those four block ids. Dirt and sand drop themselves.
Gravel dropped gravel `13`. Clay drops clay balls `337`. The cells are
`3:0`, `12:0`, `13:0`, and `82:0` then `0:0`.

This map does not claim farmland `60`, hoe till, grass, shovel durability,
or falling sand/gravel. It is distinct from M223/M239/M218/M204 place
persistence and from M324 gold-shovel dirt alone.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+dirt3+sand12+gravel13+clay82|cause=packet14-goldshovel284|wire=packet53-air+packet21-id3+packet21-id12+packet21-id13+packet21-id337|oracle=shovel-soft-breaks-drops+cells-3-12-13-82-to-0|column=17,support=4:71:4:1:0,dirt=4:72:4:3:0->0:0,drop=packet21-3:1:0,sand=4:72:4:12:0->0:0,drop=packet21-12:1:0,gravel=4:72:4:13:0->0:0,drop=packet21-13:1:0,clay=4:72:4:82:0->0:0,drop=packet21-337:1:0,shovel=284,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`83e1acd8df0e978483bdfe1199d46021b2f5b8a4908c646ca1045c002e7228d9`.
