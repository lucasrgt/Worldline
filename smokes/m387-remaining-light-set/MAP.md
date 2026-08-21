# M387 behavior map

One official session places glowstone item `89`, jack-o-lantern item `91`,
and torch item `50` on one raised stone fixture. Packet15 writes Overworld
glowstone `89:0` on the support top, look-yaw `-90` jack-o-lantern `91:1`
on the west pad, and floor torch `50:5` on the east pad. All three light
cells survive a clean save plus fresh login.

This map does not re-qualify the shipping torch-only (M175), jack
place-only (M190), glowstone place-only (M191), or jack-craft (M356)
traces. Headless `B173WireClient` protocol-14 only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+glowstone89+jackolantern91+torch50|cause=packet15-item89+packet15-item91+look-90+packet15-item50|wire=packet53-glowstone89:0+packet53-jackolantern91:1+packet53-torch50:5|oracle=live-light-set89+91+50+fresh-login|column=17,support=4:71:4:1:0,glowstone=4:72:4:89:0,west=3:71:4:1:0,jackolantern=3:72:4:91:1,east=5:71:4:1:0,torch=5:72:4:50:5,look=-90:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c8fb22dfee19b993ff3351bf0dfcb8de29c0975c84ee50c94848cd2d0e4c6d70`.
