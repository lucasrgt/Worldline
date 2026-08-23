<!-- worldline-map-schema=1 -->
<!-- boundary=m288-brown-wool -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=98dd28d183167f3553fde2b22ed4f84c648da9eb7ba620ff9ec066c95de722a8 -->

# M288 behavior map

Packet15 places brown wool item `35` damage `12` on a raised stone column.
The official server writes wool `35:12`, distinct from other wool metas
including white `35:0`, orange `35:1`, and green `35:13`. That exact cell
survives a clean save plus fresh login.

This map does not re-qualify white wool `35:0` or other dye colors.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:12|cause=packet15-item35:12|wire=packet53-wool35:12|oracle=live-block35:12+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:12,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`98dd28d183167f3553fde2b22ed4f84c648da9eb7ba620ff9ec066c95de722a8`.
