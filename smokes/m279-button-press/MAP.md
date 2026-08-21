# M279 behavior map

Stone button item 77 is placed on the east face of the M165 raised stone
column so Packet53 can confirm official block `77:1`. After 200 heartbeat
ticks drain the water-column NextTickList, empty-hand Packet15 presses the
button. The live oracle is powered metadata `77:9`, then the exact return
to `77:1` after the vanilla 20-tick stone-button delay. A clean save plus
fresh login keeps the unpowered button. This map freezes the press pulse,
not the M165 place.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+button77-east|cause=empty-hand-packet15-press|wire=packet53-button77:1->9->1|oracle=live-press-pulse+fresh-login-unpowered-button|column=17,support=4:71:4:1:0,button=5:71:4:77:1,off=77:1,on=77:9,released=77:1,settle=200ticks,delay=20ticks,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`910692630d2dc89d5acd515f421970042c6dd218a9f6b2fbc97883e672bd3eb7`.
