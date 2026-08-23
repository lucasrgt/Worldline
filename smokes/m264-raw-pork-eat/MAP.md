<!-- worldline-map-schema=1 -->
<!-- boundary=m264-raw-pork-eat -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c43583070f3c4185f97ebefa6a8f0a6ce3428f70ea6621d094546db1cbae4502 -->

# M264 behavior map

Official raw porkchop item `319` is eaten with Packet15 air-use (direction
`255` at `-1,255,-1`). Vanilla Beta 1.7.3 has no hunger bar; raw pork heals
three health points, capped at 20. The held stack is consumed `319:1 -> 0`.

This is not cooked pork. Cooked pork item `320` heals eight points (M259).
This is not M150 pig pork drop.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=rawpork319|cause=packet15-dir255-item319|wire=packet8-health17->20+packet103-rawpork-empty|oracle=itemfood-rawpork-heal3+stack-consume|health=17->20,heal=3,pork=319:1->0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c43583070f3c4185f97ebefa6a8f0a6ce3428f70ea6621d094546db1cbae4502`.
