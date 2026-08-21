# M229 behavior map

Packet15 places redstone ore item `73` on a raised stone column. The
official server writes unlit redstone ore `73:0`. That exact cell survives
a clean save plus fresh login.

This map does not claim glowing ore `74` or redstone-dust drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+ore73|cause=packet15-item73|wire=packet53-ore73:0|oracle=unlit-block73:0+fresh-login|column=17,support=4:71:4:1:0,ore=4:72:4:73:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`7d3985f4b7402dfe18498e350a010e0aa42df1a87bbb12ac06d7ff9bc4803504`.
