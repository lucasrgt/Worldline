<!-- worldline-map-schema=1 -->
<!-- boundary=m243-redstone-wire -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=6df39ca0f09d2fc710e2636bbad733e4c6e4b94ce946e915ac6367443b22a45f -->

# M243 behavior map

Packet15 places redstone dust item `331` on a raised stone column. The
official server writes unpowered wire `55:0`. That exact cell survives a
clean save plus fresh login. Lever-to-wire power is not claimed.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wire55|cause=packet15-item331|wire=packet53-wire55:0|oracle=live-block55:0+fresh-login|column=17,support=4:71:4:1:0,wire=4:72:4:55:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`6df39ca0f09d2fc710e2636bbad733e4c6e4b94ce946e915ac6367443b22a45f`.
