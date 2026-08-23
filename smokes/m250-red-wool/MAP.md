<!-- worldline-map-schema=1 -->
<!-- boundary=m250-red-wool -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c3fcc1daa3851d1bcf11abcdee87a5fa5a626dc413d114ba6ffa58c0692ef726 -->

# M250 behavior map

Packet15 places red wool item `35` damage `14` on a raised stone column.
The official server writes wool `35:14`. That exact cell survives a clean
save plus fresh login. This is distinct from M197 white wool `35:0`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+wool35:14|cause=packet15-item35:14|wire=packet53-wool35:14|oracle=live-block35:14+fresh-login|column=17,support=4:71:4:1:0,wool=4:72:4:35:14,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c3fcc1daa3851d1bcf11abcdee87a5fa5a626dc413d114ba6ffa58c0692ef726`.
