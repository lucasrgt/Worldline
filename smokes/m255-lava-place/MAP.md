# M255 behavior map

Lava bucket `327` places confined still lava `11:0` into a raised stone
basin. The actor stands on the south wall, not in the basin cell. Packet15
on that stone floor, including direction-255 raytrace, lets official
`ItemBucket` write still lava. The source cell becomes `11:0` and Packet103
replaces the selected hotbar stack with empty bucket `325:1:0`. The filled
basin and empty bucket survive a clean save plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-basin+lava-bucket327|cause=packet15-basin-cell+packet15-dir255-bucket327|wire=packet53-lava11+packet103-bucket325|oracle=live-place+fresh-login-still-lava+empty-bucket|column=17,floor=4:71:4:1:0,lava=4:72:4:0:0->11:0,held=327:1:0->325:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`62a96d88efc9d70f9cef9dc52f4555dbbaf332fb3b578a2c210abfa722ade72d`.
