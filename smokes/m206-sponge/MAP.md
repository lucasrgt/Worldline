<!-- worldline-map-schema=1 -->
<!-- boundary=m206-sponge -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=f403158d252cd74d08246cef3eee7b0ea15f96a1c88448ec6ce62d608093a441 -->

# M206 behavior map

Packet15 places sponge item `19` on a raised stone column. The official
server writes sponge `19:0`. That exact cell survives a clean save plus
fresh login.

This map does not claim water absorption. Beta 1.7.3 sponge does not
absorb water like later editions.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+sponge19|cause=packet15-item19|wire=packet53-sponge19:0|oracle=live-block19:0+fresh-login|column=17,support=4:71:4:1:0,sponge=4:72:4:19:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f403158d252cd74d08246cef3eee7b0ea15f96a1c88448ec6ce62d608093a441`.
