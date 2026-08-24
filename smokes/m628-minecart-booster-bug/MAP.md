<!-- worldline-map-schema=1 -->
<!-- boundary=minecart-booster -->
<!-- nonclaims=powered-rail-speed,furnace-cart-propulsion,riding,derailment,arbitrary-cart-collisions -->
<!-- frozen-trace=0cab886f076b9b7a6e9d9de70999dc4e1867ee6b970cbbe6444de3d6b65d4d57 -->

# M628 minecart booster behavior map

The official Beta 1.7.3 dedicated server runs a fixed-seed raised fixture with two straight
north-south tracks. Two empty minecarts begin at the same longitudinal coordinate with their
centers one block apart. Both stand on regular north-south rail `66:0`. Stone walls prevent
either cart from escaping north.

Packet7 attacks only the driver toward the south. Protocol-14 Packet31, Packet33, or Packet34
must then carry positive south displacement for both distinct Packet23 type-10 entities,
including the untouched cart on the parallel rail. The
reusable TestKit fixture normalizes entity IDs and packet choice while requiring the one-block
parallel gap and forward movement of both carts.

Frozen signal:

```text
driver=type10+forward,booster=type10+forward,parallel-gap=1,driver-rail=66:0,booster-rail=66:0,push=packet7-attack,clients=1,disconnect=clean
```

This boundary does not claim a speed multiplier, furnace-cart propulsion, riding, derailment, or
arbitrary minecart collision behavior.

Frozen trace SHA-256: `0cab886f076b9b7a6e9d9de70999dc4e1867ee6b970cbbe6444de3d6b65d4d57`.
