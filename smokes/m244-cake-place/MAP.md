<!-- worldline-map-schema=1 -->
<!-- boundary=m244-cake-place -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=3c7fa802b04a3eee353f1129e19f03bc24418ba2dfdab4b7598ebb480edc23fd -->

# M244 behavior map

Packet15 places cake item `354` on a raised stone column. The official
server writes uneaten cake `92:0`. That exact cell survives a clean save
plus fresh login. Cake eat/bite is M160, not this map.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+cake92|cause=packet15-item354|wire=packet53-cake92:0|oracle=live-block92:0+fresh-login|column=17,support=4:71:4:1:0,cake=4:72:4:92:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`3c7fa802b04a3eee353f1129e19f03bc24418ba2dfdab4b7598ebb480edc23fd`.
