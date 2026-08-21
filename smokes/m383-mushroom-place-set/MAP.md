# M383 behavior map

One official session places brown mushroom item `39` on dirt inside a dark
stone pocket and red mushroom item `40` on netherrack inside that same
roofed pocket. Packet15 writes blocks `39:0` and `40:0`. Both cells survive
a clean save plus fresh login.

This map does not re-qualify the shipping 1:1 brown-only (M200) or red-only
(M201) traces. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=dark-dirt3+dark-netherrack87+mushroom39+mushroom40|cause=packet15-item39+packet15-item40|wire=packet53-brown-mushroom39:0+packet53-red-mushroom40:0|oracle=live-block39:0+live-block40:0+fresh-login|column=17,dirt=4:72:4:3:0,brown=4:73:4:39:0,netherrack=5:72:4:87:0,red=5:73:4:40:0,roof=4:74:4:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`3a737afbb664a8e1a32858a9e371ced9062f5a30950b631a9372829456fa9a21`.
