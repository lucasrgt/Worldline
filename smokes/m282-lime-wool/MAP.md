<!-- worldline-map-schema=1 -->
<!-- boundary=m282-lime-wool -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=9b8eadf13d246083c150829c5914921e95f5f55d4dadc11e35ae365d392615c6 -->

# M282 behavior map

Packet15 places lime wool item `35` damage `5` on a raised stone column.
The official server writes wool `35:5`, distinct from M253 green `35:13`.
That exact cell survives a clean save plus fresh login.

This map does not re-qualify green wool `35:13` or other dye colors.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:5|cause=packet15-item35:5|wire=packet53-wool35:5|oracle=live-block35:5+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:5,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`9b8eadf13d246083c150829c5914921e95f5f55d4dadc11e35ae365d392615c6`.
