<!-- worldline-map-schema=1 -->
<!-- boundary=m249-yellow-wool -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=1aa0065907c89647235eddd412bad95e322d6ecd1ecfdb97dfdd1a8a7f20e599 -->

# M249 behavior map

Packet15 places yellow wool item `35` damage `4` on a raised stone column.
The official server writes wool `35:4`. That exact cell survives a clean
save plus fresh login. This is distinct from M197 white wool `35:0`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:4|cause=packet15-item35:4|wire=packet53-wool35:4|oracle=live-block35:4+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:4,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1aa0065907c89647235eddd412bad95e322d6ecd1ecfdb97dfdd1a8a7f20e599`.
