<!-- worldline-map-schema=1 -->
<!-- boundary=m275-cactus-damage -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=c708ae878b6079760d5c246f952ca1789d98c31e395a568ad9c1a2d751ef6df8 -->

# M275 behavior map

A raised stone column receives sand `12:0`. Packet15 then plants cactus item
`81` as block `81:0`. The actor moves into that cactus AABB so official
collision deals one unarmored cactus hit. Packet38 status 2 precedes Packet8
health `20 -> 19`. The cactus and health 19 survive a clean save plus fresh
login.

This is not M167 cactus placement. M167 only proves the planted `81:0` cell.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-sand12+cactus81|cause=move-into-cactus-aabb|wire=packet38-status2-before-packet8-health20->19|oracle=cactus-collision-damage|column=17,sand=4:72:4:12:0,cactus=4:73:4:81:0,health=20->19,damage=1,status=2,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`c708ae878b6079760d5c246f952ca1789d98c31e395a568ad9c1a2d751ef6df8`.
