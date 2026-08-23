<!-- worldline-map-schema=1 -->
<!-- boundary=m256-chest-minecart -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=77d7cc9f33cf75c87ba161f4e0b38376562e8c3a4a1bed0d9a78aaca8f9d0a74 -->

# M256 behavior map

The fixture raises an isolated stone column and places rail `66:0`. Using
chest-minecart item `342` on that rail emits official Packet23 type `11` with
thrower `0` and a quantized pose at the rail center. This is distinct from
M155 regular minecart type `10` / item `328`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-rail66|cause=packet15-chestminecart342|wire=packet23-type11+thrower0|oracle=two-peer-identical-chest-minecart-object|column=17,rail=4:72:4:66:0,cart=type11+shared-positive-id+thrower0+fixed144:2331:144,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`77d7cc9f33cf75c87ba161f4e0b38376562e8c3a4a1bed0d9a78aaca8f9d0a74`.
