# M247 behavior map

Packet15 places wood/log item `17` damage `2` on a raised stone column.
The official server writes birch log `17:2`. That exact upright cell
survives a clean save plus fresh login.

This map is distinct from oak `17:0` and spruce `17:1`. It does not claim
later axis metadata unless the official server writes them from item
`17:2`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+log17:2|cause=packet15-item17:2|wire=packet53-log17:2|oracle=live-block17:2+fresh-login|column=17,support=4:71:4:1:0,log=4:72:4:17:2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`d2edaaf83b9d8b74ec8d46d00e40f224fc12335a5a7e9fd35e7744a490781eb0`.
