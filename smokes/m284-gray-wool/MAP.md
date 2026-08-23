<!-- worldline-map-schema=1 -->
<!-- boundary=m284-gray-wool -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=73e9c154cc10de9ba90cb2af73ce28ad87ed76e593fc4961f12616d08161821c -->

# M284 behavior map

Packet15 places gray wool item `35` damage `7` on a raised stone column.
The official server writes wool `35:7`, distinct from light-gray `35:8`
and black `35:15`. That exact cell survives a clean save plus fresh login.

This map does not re-qualify light-gray wool `35:8` or black wool `35:15`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:7|cause=packet15-item35:7|wire=packet53-wool35:7|oracle=live-block35:7+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:7,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`73e9c154cc10de9ba90cb2af73ce28ad87ed76e593fc4961f12616d08161821c`.
