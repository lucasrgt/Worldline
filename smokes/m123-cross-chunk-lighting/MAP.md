# M123 behavior map

The actor loads adjacent chunks `(0,0)` and `(1,0)`, then selects a solid
support at global edge coordinate `x=15`. Both target cells are exact water
`9:0` with block light zero. Packet15 replaces the source-side water with
glowstone `89:0`; Packet53 confirms the block and forty heartbeats settle the
official light engine.

A fresh login receives complete Packet51 snapshots for both chunks. The source
sample becomes 15, the water sample at `x=16` becomes 12, 55 block-light cells
increase in the source chunk and 19 increase in the neighbor. Both skylight
planes have zero deltas.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|seam=chunks-0:1,0|intervention=packet15-glowstone89-replaces-edge-water|confirmation=packet53|settle=40ticks|observation=fresh-login-packet51-both-chunks|source=15:55:6:9:0->89:0,neighbor=16:55:6:9:0,samples=0->15/0->12,leftBlock=55:55:0:15:828597af1239c58395276a7dd8358ba0d2222dfd7c69dd650e6208a7dba0f4b2,rightBlock=19:19:0:12:25087cb12dacda04f56f54932166401dd1dc2ff4a1d6e8cb99cc8dacf91867cd,leftSky=0:0:0:0:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855,rightSky=0:0:0:0:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855|disconnect=clean
```

SHA-256: `7f93c32c82a360dcdc5c546f69838e8fcbc8a221bf8ad2961bd532876608365a`.
