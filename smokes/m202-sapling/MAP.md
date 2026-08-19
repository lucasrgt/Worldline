# M202 behavior map

Packet15 places oak sapling item `6` on dirt `3` capping the M175 raised
stone column. The official server writes sapling `6:0`. That exact cell
survives a clean save plus fresh login.

This map does not wait for tree growth and does not claim bone meal.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+sapling6|cause=packet15-item6|wire=packet53-sapling6:0|oracle=live-block6:0+fresh-login|column=17,dirt=4:72:4:3:0,sapling=4:73:4:6:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`7772115ec090ef211b01204fa558371ea9983994367b0ceb0899a44441bdb24d`.
