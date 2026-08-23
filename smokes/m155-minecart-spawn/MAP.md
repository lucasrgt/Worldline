<!-- worldline-map-schema=1 -->
<!-- boundary=m155-minecart-spawn -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=8bbf2ce26b50b36cdb15763b126864882c8e138b89113c1fe6dcd75988703fab -->

# M155 behavior map

The fixture raises an isolated stone column and places rail `66:0`. Using
minecart item `328` on that rail emits official Packet23 type `10` with thrower
`0` and a quantized pose at the rail center.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-rail66|cause=packet15-minecart328|wire=packet23-type10+thrower0|oracle=two-peer-identical-minecart-object|column=17,rail=4:72:4:66:0,cart=type10+shared-positive-id+thrower0+fixed144:2331:144,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8bbf2ce26b50b36cdb15763b126864882c8e138b89113c1fe6dcd75988703fab`.
