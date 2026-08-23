<!-- worldline-map-schema=1 -->
<!-- boundary=m407-chicken-egg-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=a27d5e84d4fc5e08292a9a78c2ebccf8027e9441118ed789ef3adc30d8ff97a6 -->

# M407 behavior map

The fixture raises an isolated `7×7` grass platform and places one default
mob spawner `52:0`. After a clean save the region NBT `EntityId` is rewritten
from `Pig` to `Chicken`. Packet24 type `93` is required. A 40-tick bound
peeks for laid Packet21 egg `344`. Because vanilla laying is `6000+` ticks,
the session then air-uses family item `344` and requires Packet23 type `62`
while the chicken entity is still in the same session.

This map does not re-qualify M169/M331 thrown eggs without a chicken, and
it does not claim hatch, feather `288`, or M389 chicken death drops.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-7x7-grass-platform+chicken-spawner52+egg344|cause=packet24-type93+bounded-packet21-344+packet15-dir255-egg344|wire=packet24-type93+packet23-type62|oracle=chicken-identity-plus-egg-344|column=17,platform=7x7-48grass,spawner=4:72:4:52:0,mob=type93,egg=344+packet23-type62,laid=bounded,clients=1,disconnect=clean
```

Frozen semantic SHA-256:
`a27d5e84d4fc5e08292a9a78c2ebccf8027e9441118ed789ef3adc30d8ff97a6`.
