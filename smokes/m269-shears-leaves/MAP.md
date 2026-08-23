<!-- worldline-map-schema=1 -->
<!-- boundary=m269-shears-leaves -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ea2a38e965fa7a2b9bf0d278cfd600efa328aba31fa9c3c991c85eb408a2953e -->

# M269 behavior map

Packet15 places oak log item `17` on the east face of a raised stone
column, then Packet15 places two oak leaves items `18` on the top faces.
The official server writes leaf `18:8` beside log `17:0`. Packet14 while
holding shears item `359` breaks the first leaf to air and emits Packet21
stack `18:1:0`. The same Packet14 path with an empty hand breaks the
second leaf to air without a new Packet21 leaf stack.

This map does not claim sapling chance, shears durability, or other leaf
species.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+oak17+leaves18x2|cause=packet14-shears359|wire=packet53-air+packet21-id18|oracle=shears-leaf-drop-versus-bare-hand|column=17,support=4:71:4:1:0,log=5:71:4:17:0,sheared=4:72:4:18:8->0:0,bare=5:72:4:18:8->0:0,shears=359,drop=packet21-18:1:0,bare-hand=no-new-packet21-18,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`ea2a38e965fa7a2b9bf0d278cfd600efa328aba31fa9c3c991c85eb408a2953e`.
