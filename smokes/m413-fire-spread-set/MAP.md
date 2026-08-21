# M413 behavior map

Packet15 of flint-and-steel item `259` on netherrack `87` places fire `51`
in the air cell above. Face-adjacent planks `5`, leaves `18`, and wool `35`
sit at netherrack height so official `BlockFire` scheduled ticks (rate 40)
spread fire `51` into the air cells above those fuels. A log `17` south of
the leaves stops isolated-leaf decay. Placed leaves are `18:8`. Packet53
fire `51` on the three spread cells is latched because a later snapshot may
already show consume-to-air. Exact wait length is not hashed. This SET is
distinct from M343 netherrack persist plus wool consume, and it is not ice
or snow melt.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-netherrack87+planks5+leaves18+wool35+flintsteel259|cause=packet15-item259+scheduled-fire-ticks|wire=packet53-fire51-multi-cell|oracle=live-source-fire51+adjacent-plank-leaf-wool-spread+fresh-login|column=17,support=4:71:4:1:0,rack=4:72:4:87:0,flint=259,source-fire=4:73:4:51,plank-fire=3:73:4:51,leaf-fire=4:73:5:51,wool-fire=5:73:4:51,fuels=5+18+35,spread-steps=3,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`e8fdef86a6fe2bd49b4575a296bc67cfe62dce1f2eb89aefd7ca2e89aa70843c`.
