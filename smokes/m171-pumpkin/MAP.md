# M171 behavior map

Packet15 places pumpkin item `86` on a raised stone column. Actor look
Packet12 yaw `-90` causes the official server to write pumpkin `86:1`.
That exact cell survives a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+pumpkin86|cause=packet15-item86+look-90|wire=packet53-pumpkin86:1|oracle=look-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,pumpkin=4:72:4:86:1,look=-90:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`239e38d65add8b63f1afa9dba90e1832f5b70b91bf6b6e5fee0df381e48c12e0`.
