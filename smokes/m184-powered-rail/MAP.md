# M184 behavior map

Packet15 places powered-rail item `27` on a raised stone column. The
official server writes unpowered `27:0` (north-south in the low 3 bits;
powered bit 8 stays 0 without redstone). That exact cell survives a
clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+powered-rail27|cause=packet15-item27|wire=packet53-rail27:0|oracle=unpowered-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,rail=4:72:4:27:0,powered=0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`d017b9f94e15a87ad2465679091958308c683a77948158ee7b9b3fb241c56264`.
