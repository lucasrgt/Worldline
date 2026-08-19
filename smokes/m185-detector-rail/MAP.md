# M185 behavior map

Packet15 places detector rail item `28` on a raised stone column. The official
server writes unpowered detector rail `28:0`. No minecart is present, so the
detector bit stays 0. That exact cell survives a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+detector28|cause=packet15-item28|wire=packet53-detector28:0|oracle=unpowered-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,rail=4:72:4:28:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`81cc57ce0d8d5c637a58696af5d3d47097bd3d14016813703c4f7718cb9505a2`.
