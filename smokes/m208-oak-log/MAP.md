# M208 behavior map

Packet15 places wood/log item `17` damage `0` on a raised stone column.
The official server writes oak log `17:0`. That exact upright cell
survives a clean save plus fresh login.

This map does not claim birch `17:2`, spruce `17:1`, or later axis
metadata unless the official server writes them from item `17:0`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+log17:0|cause=packet15-item17:0|wire=packet53-log17:0|oracle=live-block17:0+fresh-login|column=17,support=4:71:4:1:0,log=4:72:4:17:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c371df4ca97d388218b9184b3b6a0ba2745803de01514b4b8562c6ca33b533d2`.
