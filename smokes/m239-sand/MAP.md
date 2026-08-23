<!-- worldline-map-schema=1 -->
<!-- boundary=m239-sand -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=bb15230ca24298e16113b08fd83f225bd6b7966fbcdd78d7378ecc59c58e1094 -->

# M239 behavior map

Packet15 places sand item `12` on a raised stone column. The official
server writes supported sand `12:0`. That exact cell survives a clean
save plus fresh login.

This map does not claim falling-sand physics. M119 already covers gravity
after support removal.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+sand12|cause=packet15-item12|wire=packet53-sand12:0|oracle=live-block12:0+fresh-login|column=17,support=4:71:4:1:0,sand=4:72:4:12:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`bb15230ca24298e16113b08fd83f225bd6b7966fbcdd78d7378ecc59c58e1094`.
