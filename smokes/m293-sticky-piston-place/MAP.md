<!-- worldline-map-schema=1 -->
<!-- boundary=m293-sticky-piston-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=bf6cf185cefc337d8be549efbcdce76d5c7cff54669d136c1002f30b7ff25c1e -->

# M293 behavior map

Packet15 places sticky piston item `29` on a raised stone column. The official
server writes sticky piston `29:1`. That exact cell survives a clean save plus
fresh login.

This map does not claim sticky pullback (M144).

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+stickypiston29|cause=packet15-item29+look180|wire=packet53-stickypiston29:1|oracle=official-facing-metadata+fresh-login|column=17,support=4:71:4:1:0,piston=4:72:4:29:1,look=180:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`bf6cf185cefc337d8be549efbcdce76d5c7cff54669d136c1002f30b7ff25c1e`.
