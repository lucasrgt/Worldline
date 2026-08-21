# M294 behavior map

Packet15 places piston item `33` on a raised stone column. Actor look
Packet12 yaw `180` from above the support causes the official server to write
piston `33:1`. That exact up-facing cell survives a clean save plus fresh login.

This map does not claim piston extension (M142) or sticky-piston pull (M144).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+piston33|cause=packet15-item33+look180|wire=packet53-piston33:1|oracle=look-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,piston=4:72:4:33:1,look=180:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`3fa31fff0d03751901d6283ff022999a5d94d205d79d1a77106294cc8b041624`.
