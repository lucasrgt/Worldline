<!-- worldline-map-schema=1 -->
<!-- boundary=m579-boat-current-push-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8f1abed600a6a2597af5298ac5a9d410beadf7afc3fdd328745db2eb2236359e -->

# M579 behavior map

The fixture raises an isolated stone channel, places still water `9:0` at
the west end, opens a dirt gate so flowing water `8:2` occupies the interior
cell, and uses boat item `333` in-air (Packet15 direction 255) while standing
in that flowing cell. Two peers decode the same protocol-14 Packet23 boat
object type observed from the official server, then Packet31, Packet33, or
Packet34 motion whose quantized X increases with the eastward current.

This map does not claim still-water spawn (M154), ride attach/detach
(M378), or wreckage drops (M403). Headless `B173WireClient` protocol-14
only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-channel+flowing-water8|cause=packet15-dir255-boat333|wire=packet23-type1+packet31-or33-or34-downstream|oracle=two-peer-boat-current-push-not-spawn-only-not-ride-not-break|column=17,source=4:72:4:9:0,flow=6:72:4:8:2,boat=type1+shared-id+packet23+packet31|33|34-downstream,spawn=208:2313:144,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8f1abed600a6a2597af5298ac5a9d410beadf7afc3fdd328745db2eb2236359e`.
