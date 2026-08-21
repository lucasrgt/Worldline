# M283 behavior map

Packet15 places pink wool item `35` damage `6` on a raised stone column.
The official server writes wool `35:6`, distinct from M197 white `35:0`.
That exact cell survives a clean save plus fresh login.

This map does not re-qualify white wool `35:0` or other dye colors.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:6|cause=packet15-item35:6|wire=packet53-wool35:6|oracle=live-block35:6+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:6,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`54b69bd3555ea1c71c7bfe4a627ef3aebad41301df717ac15f1338148b198863`.
