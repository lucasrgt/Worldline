# M181 behavior map

Empty bucket `325` picks up confined still lava `11:0` from a raised stone
basin. The actor stands on the south wall, not in the lava cell. The source
cell becomes air `0:0` and Packet103 replaces the selected hotbar stack with
lava bucket `327:1:0`. The empty basin and filled bucket survive a clean save
plus fresh login.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone-basin+still-lava11|cause=packet15-lava-cell+packet15-dir255-bucket325|wire=packet53-air0+packet103-bucket327|oracle=live-pickup+fresh-login-empty-basin+lava-bucket|column=17,floor=4:71:4:1:0,lava=4:72:4:11:0->0:0,held=325:1:0->327:1:0,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`8389064523049de74163fc5f5c48e14d5e52eb750aee3eb297010fa2e87116d4`.
