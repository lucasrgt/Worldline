# M120 behavior map

The constructor uses Packet15 to build a stone trench, place source block
`9:0`, and close its only horizontal exit with dirt `3:0`. After stabilization,
a new session receives the complete baseline through Packet51.

Packet14 removes the dirt and Packet53 exposes air. Official fluid updates then
publish target `9:1`. A third session's Packet51 must retain source `9:0` and
target `9:1`; the full-chunk delta admits exactly the target cell.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-trench+seeded-water-block9+dirt-gate3|settle=200+40ticks|cause=packet14-open-horizontal-cell|confirmation=packet53-air|effect=official-horizontal-water|observation=live-packet53+fresh-login-packet51|column=10,source=4:65:4:9:0,target=5:65:4:3:0->0:0->9:1,states=1:b04b6593e9708c471e970cac23a8f32913f3de2300e56b463a1a53638c8ffc62|disconnect=clean
```

SHA-256: `c0bbf83eadc6fd56c3697b50ed2d653aebc2fd9e132467354a9bcae89a6daa29`.
