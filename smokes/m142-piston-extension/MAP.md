# M142 behavior map

The fixed-seed fixture builds a ten-block stone column above generated water.
With yaw `-90`, ordinary Packet15 placement creates west-facing normal piston
`33:4`. A stone `1:0` occupies its front cell and side lever `69:1` powers the
support.

One later Packet15 lever activation produces the official settled states
`69:9`, `33:12`, piston head `34:4`, and displaced stone `1:0`. The four-cell
raised-state digest is calculated over every cell from support Y through 127,
not over the generated water below the tower. A fresh login Packet51 must
contain all four final states.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+piston33-west+stone1+lever69|settle=200+10ticks|cause=packet15-lever-activate|effect=official-piston-event+stone-displacement|observation=fresh-login-packet51|column=10,lever=5:64:4:1->9,piston=4:65:4:4->12,head=1:0->34:4,pushed=0:0->1:0,raised-states=4:0c19d14780adcd86f79c22be952ef74e05540a4784e3e71812a4c9d2230d0cd5|disconnect=clean
```

Frozen semantic SHA-256:
`48c199a75f4cb6d77ffd1cfec3081c5fa9880915553d5b7e913ddc7cb6a38a20`.
