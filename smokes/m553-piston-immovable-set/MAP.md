# M553 behavior map

The cloned M146 west-facing piston family occupies one raised stone column.
Three normal pistons `33:4` sit on the support row with chest `54:0`, furnace
`61:4`, and mob spawner `52:0` immediately west and air in each destination
cell. One lever activation per arm powers that lever from metadata `1` to
`9`. Each piston remains `33:4`, each payload stays in place, and each
destination remains `0:0`.

This map is distinct from M146 obsidian-only (`49:0`) and from M147's
twelve-block push-limit pair. Fresh login Packet51 keeps all three final
arms.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=piston33-west+chest54+furnace61+spawner52|settle=200+10ticks|cause=packet15-lever-activate|effect=official-piston33-immovable-set|observation=fresh-login-packet51|column=10,chest=54:0->54:0,furnace=61:4->61:4,spawner=52:0->52:0,piston33=4->4,chest-arm=4:65:4:33:4->4,payload=3:65:4:54:0->54:0,dest=2:65:4:0:0->0:0,furnace-arm=4:65:6:33:4->4,payload=3:65:6:61:4->61:4,dest=2:65:6:0:0->0:0,spawner-arm=4:65:8:33:4->4,payload=3:65:8:52:0->52:0,dest=2:65:8:0:0->0:0,retracted=true,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`6b35bf7c4b6f658370491bc20505538a93425b8309bc17d26f9d8b3d19ff06cf`.
