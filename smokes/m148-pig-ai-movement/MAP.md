<!-- worldline-map-schema=1 -->
<!-- boundary=m148-pig-ai-movement -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c265a9aa7d1e6254b11458774346f05613c82569948443803f4742740e933397 -->

# M148 behavior map

One official default spawner creates a pig identity shared by two peers. After
that Packet24 is selected, each peer applies signed relative Packet31/33 or
absolute Packet34 coordinates to the same fixed-point state and returns the
same first horizontal transition.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+default-spawner52|cause=official-pig-ai-tick|wire=packet31-or33-or34-position-transition|oracle=two-peer-identical-quantized-horizontal-movement|column=17,platform=7x7-48grass,spawner=52:0,mob=type90+shared-id,movement=shared-horizontal-nonzero+packet31|33|34+quantized32+within8x3x8,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c265a9aa7d1e6254b11458774346f05613c82569948443803f4742740e933397`.
