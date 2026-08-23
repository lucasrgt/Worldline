# M606 behavior map

Packet15 of flint-and-steel item `259` on netherrack `87` places fire `51`
in the air cell above. An 8-cell ring of planks `5` plus wood `17` sits at
netherrack height so official random ticks write Packet53 fire `51` into at
least one air cell above those fuels. A stone sky cover sits above the pad
so spread is not rain-gated. The source fire stays `51`. Exact wait length
and which wood-adjacent air cell ignites are not hashed.

This map does not claim M268 flint-and-steel place, M515 fire-support
extinguish, rain extinguishing, leaves, or wool.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-netherrack87+planks5-ring+wood17+cover1+flintsteel259|cause=packet15-item259+random-ticks|wire=packet53-fire51-spread-air|oracle=live-source-fire51+adjacent-wood-plank-spread+fresh-login|column=17,support=4:71:4:1:0,rack=4:72:4:87:0,flint=259,source-fire=4:73:4:51,wood-ring=8,cover=4:76:4:1:0,fuels=5+17,spread=air->51,source-stay=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`4b6a960897e496015d385c4ad2f648d15557860809b692c0e544595f6635f9bc`.
