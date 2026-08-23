<!-- worldline-map-schema=1 -->
<!-- boundary=m285-light-gray-wool -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f98cb91704701be85feaa966d2fbe24aa8b5b4df58daeabe7fc7a799836f7ae5 -->

# M285 behavior map

Packet15 places light-gray wool item `35` damage `8` on a raised stone
column. The official server writes wool `35:8`, distinct from gray
`35:7`. That exact cell survives a clean save plus fresh login.

This map does not re-qualify gray wool `35:7`, white wool `35:0`, or
other dye colors.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:8|cause=packet15-item35:8|wire=packet53-wool35:8|oracle=live-block35:8+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:8,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f98cb91704701be85feaa966d2fbe24aa8b5b4df58daeabe7fc7a799836f7ae5`.
