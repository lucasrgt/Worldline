<!-- worldline-map-schema=1 -->
<!-- boundary=m214-diamond-block -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8915ac3e6a21fca55c8386b8affce516f9b29aa3d4fcb00dca48fec0fb8f4eed -->

# M214 behavior map

Packet15 places diamond block item `57` on a raised stone column. The official
server writes diamond block `57:0`. That exact cell survives a clean save
plus fresh login.

This map does not claim diamond ore `56` or diamond item `264`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+diamond57|cause=packet15-item57|wire=packet53-diamond57:0|oracle=live-block57:0+fresh-login|column=17,support=4:71:4:1:0,diamond=4:72:4:57:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8915ac3e6a21fca55c8386b8affce516f9b29aa3d4fcb00dca48fec0fb8f4eed`.
