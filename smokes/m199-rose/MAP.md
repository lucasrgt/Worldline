<!-- worldline-map-schema=1 -->
<!-- boundary=m199-rose -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=d79a60342cee16cfece9348ecc6371263bd13bc5f50896d336b8fae6d9d750dd -->

# M199 behavior map

Packet15 places rose item `38` on dirt `3` after stone rejects the same
cell. The official server writes rose `38:0`. That exact cell survives a
clean save plus fresh login.

This map does not claim dandelion `37`, bone meal, or dye.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-dirt3+rose38|cause=packet15-item38|wire=packet53-rose38:0|oracle=live-block38:0+fresh-login|column=17,dirt=4:72:4:3:0,rose=4:73:4:38:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`d79a60342cee16cfece9348ecc6371263bd13bc5f50896d336b8fae6d9d750dd`.
