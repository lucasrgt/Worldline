# M310 behavior map

The fixture reuses the M154 still-water cell `4:60:4:9:0` for boat item `333`
and a nearby raised rail `66:0` at `4:72:5` for minecart item `328`. Packet23
type `1` and type `10` share the existing object tracker. Empty-hand Packet7
mounts VehSee310 on the boat and VehRides310 on the cart. Each rider freezes
Packet39 attach.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=natural-water9+raised-rail66|cause=packet15-dir255-boat333+empty-hand-packet7-mount+packet15-minecart328+empty-hand-packet7-mount|wire=packet23-type1+packet23-type10+packet39-attach|oracle=two-peer-vehicle-spawns+type1-and-type10-rides|water=4:60:4:9:0,boat=type1+shared-id+packet23+attach,column=17,rail=4:72:5:66:0,cart=type10+shared-positive-id+thrower0+fixed144:2331:176+attach,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`e9490bd2395a9a0e2f23738cb8956250a2a8738d5f0d1c62c27d254b43a8ff3f`.
