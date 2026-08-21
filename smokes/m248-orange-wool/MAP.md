# M248 behavior map

Packet15 places orange wool item `35` damage `1` on a raised stone column.
The official server writes wool `35:1`, distinct from M197 white `35:0`.
That exact cell survives a clean save plus fresh login.

This map does not re-qualify white wool `35:0` or other dye colors.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:1|cause=packet15-item35:1|wire=packet53-wool35:1|oracle=live-block35:1+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:1,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`89d3e625a2e980af7b569af8fc82e46b0e5ecff6f79e8090cabff02a38496590`.
