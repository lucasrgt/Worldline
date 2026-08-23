<!-- worldline-map-schema=1 -->
<!-- boundary=m234-sandstone-slab -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=607498ca23a0859d9a2296eba8fb8d4c4c407a3af84801eb6ccf552314e65f2a -->

# M234 behavior map

Packet15 places sandstone slab item `44` with damage `1` on a raised stone
column. The official server writes single slab `44:1`. That exact cell
survives a clean save plus fresh login.

This map does not re-qualify stone slab `44:0` or double slab `43`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+slab44:1|cause=packet15-item44:1|wire=packet53-slab44:1|oracle=sandstone-metadata+fresh-login|column=17,support=4:71:4:1:0,slab=4:72:4:44:1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`607498ca23a0859d9a2296eba8fb8d4c4c407a3af84801eb6ccf552314e65f2a`.
