# M246 behavior map

Packet15 places wood/log item `17` damage `1` on a raised stone column.
The official server writes spruce log `17:1`. That exact cell survives a
clean save plus fresh login.

This map is distinct from M208 oak log `17:0`. It does not claim birch
`17:2` or later axis metadata.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+log17:1|cause=packet15-item17:1|wire=packet53-log17:1|oracle=live-block17:1+fresh-login|column=17,support=4:71:4:1:0,log=4:72:4:17:1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`da7cf603b820a91005a39a8dcd6ce70f9779f145f26a8ffc835f7ad93a077693`.
