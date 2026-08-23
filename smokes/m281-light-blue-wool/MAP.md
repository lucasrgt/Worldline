<!-- worldline-map-schema=1 -->
<!-- boundary=m281-light-blue-wool -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=49e519da435b759ce7053b4105c826cbae35a31badd8bc4f4e50d0cd48617e1f -->

# M281 behavior map

Packet15 places light-blue wool item `35` damage `3` on a raised stone
column. The official server writes wool `35:3`, distinct from other wool
metas including M197 white `35:0`. That exact cell survives a clean save
plus fresh login.

This map does not re-qualify white wool `35:0` or other dye colors.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:3|cause=packet15-item35:3|wire=packet53-wool35:3|oracle=live-block35:3+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:3,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`49e519da435b759ce7053b4105c826cbae35a31badd8bc4f4e50d0cd48617e1f`.
