# M165 behavior map

Stone button item 77 is placed on the east face of a raised stone column.
Packet53 confirms official block `77:1` facing metadata. After 200 heartbeat
ticks drain the water-column NextTickList, empty-hand Packet15 presses the
button to `77:9`. sustainTicks then observes the automatic return to `77:1`
after the vanilla 20-tick stone-button delay. A clean save plus fresh login
keeps the unpowered button.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+button77|cause=packet15-item77-place+empty-hand-packet15-activate|wire=packet53-button77-facing+power-bit|oracle=live-pulse+fresh-login-unpowered-button|column=17,support=4:71:4:1:0,button=5:71:4:77:1,off=77:1,on=77:9,settle=200ticks,delay=20ticks,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`293f5014cd6d64c96de7f120544a33fdc68c2ac3843deba132c1d00ee8e00300`.
