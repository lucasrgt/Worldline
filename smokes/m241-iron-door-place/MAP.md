# M241 behavior map

Official iron `BlockDoor` (block 71) is placed from iron door item 330 onto a
raised stone support. Vanilla emits two Packet53 cells whose metadata comes
from the actor look yaw: lower `71:0` and upper `71:8` for this facing. A
clean save plus fresh login retains those halves. This map does not power the
door, toggle it by hand, or re-qualify M118.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+irondoor71|cause=packet15-item330-place|wire=packet53-door71:0/8|oracle=irondoor-place+fresh-login|column=17,support=4:71:4:1:0,lower=4:72:4:71:0,upper=4:73:4:71:8,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`a4f2a5f71fe45b70e843d41f32d6a4327eac0d654a488b059ca29eb2a2d261e6`.
