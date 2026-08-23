<!-- worldline-map-schema=1 -->
<!-- boundary=m312-torch-invert -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=e4b4e7bf13497288e3b90b76bd07f714f976ecc54254f40ae81e8150b4924ae9 -->

# M312 torch invert behavior map

Packet15 places redstone torch item `76` on the north face of an unpowered
raised stone block as lit wall torch `76:4`. A west-facing repeater then
powers that block. The official server inverts the same cell to unlit
`75:4`. Both states occur in one cycle, distinct from floor torch `76:5`.
The inverted cell survives a clean save plus fresh login.

This map does not re-qualify floor torch `76:5` or lighting.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+inverter+torch76->75|cause=packet15-item76-then-powered-block|wire=packet53-torch76:4->torch75:4|oracle=live-on+live-off+fresh-login|column=17,support=4:71:4:1:0,torch=3:72:3:76:4->75:4,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`e4b4e7bf13497288e3b90b76bd07f714f976ecc54254f40ae81e8150b4924ae9`.
