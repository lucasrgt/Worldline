<!-- worldline-map-schema=1 -->
<!-- boundary=m211-double-slab -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a5ad4aa55f65cbcb979ce018f5143d7d2036b7db34c77ed5f1369e2624fbf546 -->

# M211 behavior map

Packet15 places double stone slab item `43` on a raised stone column. The
official server writes double slab `43:0`. That exact cell survives a clean
save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+slab43|cause=packet15-item43|wire=packet53-slab43:0|oracle=double-metadata+fresh-login|column=17,support=4:71:4:1:0,slab=4:72:4:43:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a5ad4aa55f65cbcb979ce018f5143d7d2036b7db34c77ed5f1369e2624fbf546`.
