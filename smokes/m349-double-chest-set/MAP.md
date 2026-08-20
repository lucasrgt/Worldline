# M349 behavior map

Packet15 places two adjacent chest items `54` on a raised two-block stone
pad. Opening either cell with an empty hand yields Packet100 type 0,
title `Large chest`, and 54 container-owned slots, correlated with a
90-slot Packet104 view. Both chest cells survive a clean save plus
fresh login, and the reopened window is still 54 owned slots. This map
is distinct from M232 single-chest place and from M54's title `Chest`
27-slot single-chest window.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+two-adjacent-chest54|cause=packet15-item54+packet15-item54+packet15-empty|wire=packet53-chest54:0+packet53-chest54:0+packet100-readUTF-owned54-title-Large-chest|oracle=double-chest-window54+both-cells+fresh-login|column=17,support=4:71:4:1:0,east=5:71:4:1:0,left=4:72:4:54:0,right=5:72:4:54:0,window=title=Large chest,owned=54,total=90,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ec079803ad133072d794b370d1dd5988e5931287cded14a33e3abd7702c0fd26`.
