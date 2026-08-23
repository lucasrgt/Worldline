# M607 behavior map

A raised stone column receives dirt `3:0` beside still water `9:0`.
Packet15 plants sugar-cane item `338` as reed `83:0`. Packet15 of stone
`1` then replaces the hydrating water cell. Official `canBlockStay` is
already false, but the cane is diagonal to that water and stays until
Packet15 of stone against the cane east face neighbor-updates
`BlockReed`. Packet53 replaces cane with air, and Packet21 drops reed
item `338`. Fresh login Packet51 keeps cane air beside dirt `3:0` and
neighbor stone `1:0`.

This map is distinct from M159 water-adjacent sugar-cane growth and
M384 cactus plus sugar-cane height growth.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+still-water9+reed83|cause=packet15-item1-replace-water9+packet15-item1-east-neighbor|wire=packet53-reed83->0+packet21-338|oracle=dry-break-pop+fresh-login-air|column=17,dirt=4:72:4:3:0,water=5:72:4:9:0->1:0,cane=4:73:4:83:0->0:0,neighbor=5:73:4:1:0,drops=packet21-338,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`fc510e49797e7209a3a92a1d34c5ddd918cbfc34b4942d10af06278b6eaf57f4`.
