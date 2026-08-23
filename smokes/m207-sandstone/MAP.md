<!-- worldline-map-schema=1 -->
<!-- boundary=m207-sandstone -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f1168be76eb874a213a7c540fcc667aeb929883a30fe9ccb00676cf74cf65b8e -->

# M207 behavior map

Packet15 places sandstone item `24` on a raised stone column. The official
server writes sandstone `24:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim chiseled or smooth sandstone; those variants are
later editions.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+sandstone24|cause=packet15-item24|wire=packet53-sandstone24:0|oracle=live-block24:0+fresh-login|column=17,support=4:71:4:1:0,sandstone=4:72:4:24:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f1168be76eb874a213a7c540fcc667aeb929883a30fe9ccb00676cf74cf65b8e`.
