<!-- worldline-map-schema=1 -->
<!-- boundary=m287-purple-wool -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=5dc40bd722b0e06eda7a5458a94b93ca0bdccfc730d99fdbe3204f19d850a7a8 -->

# M287 behavior map

Packet15 places purple wool item `35` damage `10` on a raised stone column.
The official server writes wool `35:10`, distinct from blue `35:11` and
magenta `35:2`. That exact cell survives a clean save plus fresh login.

This map does not re-qualify blue wool `35:11` or magenta wool `35:2`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:10|cause=packet15-item35:10|wire=packet53-wool35:10|oracle=live-block35:10+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:10,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`5dc40bd722b0e06eda7a5458a94b93ca0bdccfc730d99fdbe3204f19d850a7a8`.
