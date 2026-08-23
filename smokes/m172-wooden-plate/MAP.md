<!-- worldline-map-schema=1 -->
<!-- boundary=m172-wooden-plate -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=ffcac8ad53202102f7e7ff5179823d53d8ecd116c879faba5a3c1ccf9bcd94c1 -->

# M172 behavior map

Wooden pressure plate item `72` is placed on a raised stone support as block
`72:0`. The headless actor then `moveAndObserve`s onto that cell so the official
server powers the plate to `72:1` through Packet53. Stepping off depowers it
back to `72:0`. A clean save plus fresh login rereads unpowered `72:0`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+plate72|cause=packet15-item72+moveAndObserve-on-cell|wire=packet53-plate72:0->1->0|oracle=live-power+depower+fresh-login-unpowered|column=17,support=4:71:4:1:0,plate=4:72:4:72:0->1->0,persisted=72:0,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ffcac8ad53202102f7e7ff5179823d53d8ecd116c879faba5a3c1ccf9bcd94c1`.
