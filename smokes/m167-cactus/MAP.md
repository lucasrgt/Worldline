<!-- worldline-map-schema=1 -->
<!-- boundary=m167-cactus -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=9a210a58a09a40ac501c31bf8262bee7846ea1240c7dc0654766374ba627ef30 -->

# M167 behavior map

A raised stone column receives sand `12:0`. Packet15 then plants cactus item
`81` as block `81:0`. The cactus remains after a bounded live wait and a clean
save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-sand12|cause=packet15-item81-cactus|wire=packet53-cactus81|oracle=live-hold+fresh-login-cactus|column=17,sand=4:72:4:12:0,cactus=4:73:4:81:0,hold=40ticks,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`9a210a58a09a40ac501c31bf8262bee7846ea1240c7dc0654766374ba627ef30`.
