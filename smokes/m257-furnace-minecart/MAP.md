<!-- worldline-map-schema=1 -->
<!-- boundary=m257-furnace-minecart -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=57acb0174de88e73ae6725e8a676aa2dffb0d4b73fe19cbe462be5d882a70264 -->

# M257 behavior map

The fixture raises an isolated stone column and places rail `66:0`. Using
furnace-minecart item `343` on that rail emits official Packet23 type `12`
with thrower `0` and a quantized pose at the rail center. Two peers observe
the same entity identity. Type `12` is not M155 type `10` or M256 type `11`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-rail66|cause=packet15-furnace-minecart343|wire=packet23-type12+thrower0|oracle=two-peer-identical-furnace-minecart-object|column=17,rail=4:72:4:66:0,cart=type12+shared-positive-id+thrower0+fixed144:2331:144,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`57acb0174de88e73ae6725e8a676aa2dffb0d4b73fe19cbe462be5d882a70264`.
