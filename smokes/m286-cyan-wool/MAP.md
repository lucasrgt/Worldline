# M286 behavior map

Packet15 places cyan wool item `35` damage `9` on a raised stone column.
The official server writes wool `35:9`, distinct from light-blue `35:3`
and blue `35:11`. That exact cell survives a clean save plus fresh login.

This map does not re-qualify light-blue wool `35:3` or blue wool `35:11`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:9|cause=packet15-item35:9|wire=packet53-wool35:9|oracle=live-block35:9+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:9,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`1632e3056edc9c3fa6a76285a528128698313979964d10cedb2b03544c838e61`.
