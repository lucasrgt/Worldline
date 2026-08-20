# M274 behavior map

The official fixture stabilizes stone `1:0` at `(4,64,4)` with gravel `13:0`
directly above it, the M218 gravel identity. Packet14 removes the stone
and Packet53 must expose air at the lower coordinate before settlement.

The server's falling-gravel behavior then places `13:0` in the lower cell
and leaves `0:0` in the former upper cell. After clean disconnect/save, a
fresh Packet51 must expose both states. The ordered full-chunk delta
admits exactly those two cells. This is distinct from M119 falling sand
`12:0`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+supported-gravel13|settle=40+40ticks|cause=packet14-remove-support|confirmation=packet53-air|effect=official-falling-gravel-settle|observation=live-packet53+fresh-login-packet51|column=10,lower=4:64:4:1:0->13:0,upper=4:65:4:13:0->0:0,states=2:a919f7bd5ed11b66e9dfd6fb45f0e12ca9da52352113f3021401560c8c57c2e4|disconnect=clean
```

SHA-256: `176ae1fac3a1eb0fc755149f750defb1e9bf184c097416e0d6f216e41c7fb222`.

Packet14 is request evidence; Packet53 air is the support-removal boundary.
The settled live state and reload Packet51 are the gravity outcome oracles.
