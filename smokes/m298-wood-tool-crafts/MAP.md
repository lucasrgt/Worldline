# M298 behavior map

Packet15 places workbench item `58` on a raised stone column. A second
empty-hand Packet15 opens that table. Packet100 declares the 3x3
`Crafting` window. Packet102 writes oak planks `5` and sticks `280` in
the wooden-tool shapes, and Packet106 accepts each click. Result slot 0
yields wooden sword `268`, pickaxe `270`, axe `271`, shovel `269`, and
hoe `290`. Those items survive a clean save plus fresh login.

This map is distinct from M210 plank placement and from a single-tool
M321 wooden-sword craft. It does not claim combat, durability, or tilling.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+workbench58+planks5x11+sticks280x9|cause=packet15-open+packet102-wood-tool-family|wire=packet100-crafting+packet106-accepted|oracle=result-items268+270+271+269+290+take+fresh-login|column=17,support=4:71:4:1:0,workbench=4:72:4:58:0,results=268+270+271+269+290,taken=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`2b099c580ef169af939546718df1c4ae560e5f875f92960733fbcc026a3982bf`.
