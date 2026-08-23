<!-- worldline-map-schema=1 -->
<!-- boundary=m465-env-death-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=5f7c771e9c67210afff3c4f9afc8af6700507b0ce7d74239763d335b27bdf1b4 -->

# M465 environmental death set map

A raised stone column hosts two-deep still water `8/9` over the head, falling
sand `12` on the head cell, and still lava `11:0`. Three official logins each
start at Packet8 health 20 and wait until Packet8 health is non-positive:

- submerged-eye drowning to Packet8 health 0
- falling-sand head-cell suffocation to Packet8 health 0
- still lava `11:0` to Packet8 health 0

Lava is last so lingering fire cannot contaminate the other two deaths. This
is distinct from M307's hurt-only `20 -> 19` family, from M461 fall, and from
M469 void.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+two-still-water+falling-sand12+still-lava11|cause=submerged-eye-air-deplete+stand-under-falling-sand12+stand-in-lava|wire=packet38-status2+packet8-health20->0|oracle=lava+drown+suffocate-deaths-not-m307-hurt-or-m461-fall-or-m469-void|causes=lava+drown+suffocate,column=17,lava=4:72:2:11:0,water=4:72:4+4:73:4,head=4:73:6:12:0,deaths=drown:20->0+suffocate:20->0+lava:20->0,packet8=0,status=2,logins=3,disconnect=clean
```

Frozen semantic SHA-256:
`5f7c771e9c67210afff3c4f9afc8af6700507b0ce7d74239763d335b27bdf1b4`.
