# M378 behavior map

One official session places boat item `333` through Packet15 direction 255
on the M154 still-water cell `4:60:4:9:0`. Two peers decode the same
protocol-14 Packet23 type `1` at quantized pose `144:1993:144`. Empty-hand
Packet7 mounts BoatWater378 and Packet39 attach freezes on the existing
object tracker. A second empty-hand Packet7 toggles the official unmount
and Packet39 vehicle `-1` is the detach.

This map does not re-qualify shipping spawn-only (M154) or craft-only
(M326) traces. Headless `B173WireClient` only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=natural-water9|cause=packet15-dir255-boat333+empty-hand-packet7-mount+empty-hand-packet7-unmount|wire=packet23-type1+packet39-attach+packet39-detach|oracle=two-peer-identical-boat-spawn+ride-then-detach+not-craft|water=4:60:4:9:0,boat=type1+shared-id+packet23+attach+detach,pose=144:1993:144,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`bdd585b5e79c816f4761039c63a02aa8e9f6164e77d7baa4fa4b3980a6a8d905`.
