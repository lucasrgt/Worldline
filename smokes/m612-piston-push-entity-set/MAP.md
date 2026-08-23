# M612 behavior map

The cloned west-facing piston family occupies one raised stone column with
floors under an air head cell. Piston `33:4` sits on the support at
`(4,65,4)`. Stone floors occupy the west cells at support Y so a cobble
item can rest in the air head instead of falling into water. Lever `69`
sits on the east face. This payload is an entity, not a moved block.

Packet14 status 4 drops cobble `4x1:0` into the air head cell. Empty-hand
Packet15 then extends piston `33` (`33:4 -> 33:12`, head `34:4`,
destination air). Fresh login Packet21 keeps the cobble stack at a
west-shifted coordinate (`dx-west=true`). Fresh login Packet51 keeps the
extended arm.

This map is distinct from M142/M367 stone-block motion, M546 QC, and
M293/M294 place-only facings. It does not claim sticky pull, BUD, or a
player-pose rubber-band.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=piston33-west+air-head+cobble-item4+floors|settle=20+10ticks|cause=packet14-status4-cobble+packet15-lever-activate|effect=official-piston33-extend+item-entity-west|observation=fresh-login-packet21+packet51|column=10,extend=33:4->12,head=0:0->34:4,item=4x1:0,dx-west=true,piston=4:65:4:33:4->12,head-cell=3:65:4:0:0->34:4,pushed=2:65:4:0:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`f34c4db7ef2aa8add3324a61994478d128f556aa31a3f55a6e70bdc348377f3f`.
