# M251 behavior map

Packet15 places black wool item `35` damage `15` on a raised stone column.
The official server writes wool `35:15`. That exact cell survives a clean
save plus fresh login. This path is distinct from M197 white wool `35:0`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:15|cause=packet15-item35:15|wire=packet53-wool35:15|oracle=live-block35:15+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:15,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b046e1d6723ba4e19db7d84e49aafbd1dd701fc696e2ce2f6754ad839f4a23be`.
