<!-- worldline-map-schema=1 -->
<!-- boundary=m340-redstone-input-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=366f2922f527ce87c4818902a75b3e646c8a6e5946e6b84838fe3c9918f0c456 -->

# M340 behavior map

Packet15 places lever `69` on the east face of a raised stone column and
stone button `77` on the east face of a south pad. Empty-hand Packet15
latches the lever `69:1 -> 69:9 -> 69:1`, then presses the button
`77:1 -> 77:9 -> 77:1` after the vanilla 20-tick stone-button delay. A
clean save plus fresh login keeps both unpowered.

This map claims the pair of official redstone inputs, not a lever-only
activation (M115) and not a button-only pulse (M165/M279). It does not
claim pressure plates (M295), redstone consumers, or wooden buttons.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+lever69-east+button77-east|cause=empty-hand-packet15-lever-on-off+empty-hand-packet15-button-press|wire=packet53-lever69:1->9->1+packet53-button77:1->9->1|oracle=lever-latch-on-off+button-pulse+fresh-login-unpowered|column=17,support=4:71:4:1:0,pad=4:71:5:1:0,lever=5:71:4:69:1->9->1,button=5:71:5:77:1->9->1,settle=200ticks,delay=20ticks,persisted=69:1+77:1,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`366f2922f527ce87c4818902a75b3e646c8a6e5946e6b84838fe3c9918f0c456`.
