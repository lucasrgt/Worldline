# M605 lava damage set behavior map

A raised stone basin receives still lava `11:0` from lava bucket `327`. The
headless actor then stands in that source cell. Official lava contact deals
an unarmored first hit of four, so Packet8 health drops `20 -> 16`. The next
accepted Packet8 is `16 -> 13`. Packet38 status 2 precedes those health
packets. The actor leaves the lava AABB while still alive. Health 13 survives
a clean save plus fresh login.

Vanilla typically also sets the player on fire. This map does not wait for a
void-style health-0 death.

This is distinct from M138 horizontal lava place/flow (`11:2`) and from M465
environmental death to Packet8 health 0. Headless `B173WireClient`
protocol-14 only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-basin+still-lava11|cause=packet15-lava-bucket327+stand-in-lava|wire=packet53-lava11+packet38-status2+packet8-health20->16->13|oracle=repeated-lava-damage-alive-not-m138-flow-not-m465-death-typically-fire|column=17,floor=4:71:4:1:0,lava=4:72:4:11:0,health=20->16->13,damage=4+3,hits=2,status=2,alive=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`ee5b9cd5369a69732b8def74eb28c5cb2ea094f821a491fc7e3c7c5937945a99`.
