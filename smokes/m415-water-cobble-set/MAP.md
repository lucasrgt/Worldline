# M415 behavior map

One official session builds two raised stone trenches and seeds still lava
`11:0` behind dirt gates. Packet14 opens each flow cell onto air. Official
fluid updates then publish flowing lava (moving block `10`, Packet53
stationary-flow `11:2`) into those cells. Packet15 places still water `9:0`
beside each flowed cell. Vanilla neighbor processing hardens each flowing
lava cell to cobblestone `4:0` while both lava sources remain lava sources.
Both cobble cells and both water cells survive a clean save plus fresh login.

This map does not re-qualify the shipping 1:1 still-lava obsidian reaction
(M139) or the still-lava obsidian set (M414). Headless `B173WireClient`
only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-paired-trenches+seeded-still-lava11+dirt-gates3|settle=40+70ticks|cause=packet14-open-flow-cells-then-packet15-still-water9-beside-flowing-lava|wire=packet53-air+packet53-flowing-lava10-as-11:2+packet53-water9+packet53-cobble4|oracle=two-cell-flowing-lava10-plus-water-to-cobble4-not-obsidian49|column=17,east-source=4:72:6:11:0,east-flow=5:72:6:3:0->0:0->11:2->4:0,east-water=6:72:6:9:0,south-source=4:72:8:11:0,south-flow=5:72:8:3:0->0:0->11:2->4:0,south-water=6:72:8:9:0,flowing-lava=10,moving=10:0,water=9,cobble=4,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`bf5ec9eaf7f4f9ec7cf8652c8bdef0af40a1d8fa89b618d519dc571fddc66148`.
