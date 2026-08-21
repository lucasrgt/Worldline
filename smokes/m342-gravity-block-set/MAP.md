# M342 behavior map

The official fixture stabilizes stone `1:0` at `(4,64,4)` with sand `12:0`
directly above it, and an adjacent stone at `(5,64,4)` with gravel `13:0`
above that. Packet14 removes each support. Packet53 must expose air at
the lower coordinate before settlement.

Packet23 type `70` is the falling-sand entity. Packet23 type `71` is the
falling-gravel entity. Both reuse the existing object tracker. The
server then places `12:0` and `13:0` in the lower cells and leaves
`0:0` in the former upper cells. After clean disconnect/save, a fresh
Packet51 must expose both settled results.

This is distinct from M119 sand-only and M274 gravel-only.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+supported-sand12+supported-gravel13|settle=40+40ticks|cause=packet14-remove-support|confirmation=packet53-air|effect=official-falling-sand-and-gravel-settle|observation=packet23-type70+packet23-type71+live-packet53+fresh-login-packet51|column=10,sand=4:64:4:1:0->12:0,sandUpper=4:65:4:12:0->0:0,gravel=5:64:4:1:0->13:0,gravelUpper=5:65:4:13:0->0:0,packet23=70+71,persisted=true,clients=2,disconnect=clean
```

SHA-256: `d8653266b9cdaa16b9aa3d3fc760642400d2172380078b03499aff10394c84e8`.

Packet14 is request evidence; Packet53 air is the support-removal
boundary. Packet23 type 70/71 plus the settled live state and reload
Packet51 are the gravity outcome oracles.
