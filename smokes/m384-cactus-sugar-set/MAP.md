# M384 behavior map

A raised isolated sand pad receives cactus item `81`. Grass pads beside still
water `9:0` receive sugar-cane item `338` as block `83`. Official random ticks
grow at least one cactus and one cane from height `1` to height `>= 2`. A
fresh login rereads cactus `81` and cane `83` on the same soils.

Exact wait length and extra height above 2 are not hashed. The frozen oracle
is both block identities plus categorical height `>= 2` after a clean save.

This map is distinct from M159 single cane, M167 single cactus placement, and
M305 wheat-plus-cactus-plus-cane growth. It does not claim harvest, bone meal,
height 3, or sand cane planting.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-sand12+grass2+still-water9|cause=packet15-item81-cactus+item338-reed|wire=packet53-cactus81+reed83|oracle=official-random-tick-height>=2+fresh-login|column=17,sand=2:72:4:12:0,cactus=2:73:4:81,cactus-height>=2,grass=4:72:4:2:0,water=5:72:4:9:0,cane=4:73:4:83,cane-height>=2,plants=81+83,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ebe81626228e8dc034975562ddc312713b9877d4020a97cec9b6e38884191824`.
