# M190 behavior map

Packet15 places jack-o-lantern item `91` on a raised stone column. Actor
look Packet12 yaw `-90` causes the official server to write jack-o-lantern
`91:1`. That exact cell survives a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+jackolantern91|cause=packet15-item91+look-90|wire=packet53-jackolantern91:1|oracle=look-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,jackolantern=4:72:4:91:1,look=-90:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`6d925e40f9f78a804de2b69ee8eb5107d5314dedb78a27dd72a2acb8fd53f77d`.
