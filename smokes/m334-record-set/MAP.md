<!-- worldline-map-schema=1 -->
<!-- boundary=m334-record-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b139e039c60f517453a6e8e0c3fe4f87b11f5c73faa81a77c7fceb7645428d53 -->

# M334 behavior map

Official jukebox item `84` is placed twice on a raised stone support. Gold
disc `2256` (13) is used on the first cell and green disc `2257` (cat) on
the second. The dedicated server emits Packet61 effect `1005` data `2256`
then Packet61 effect `1005` data `2257`, empties both selected slots, and
writes metadata `84:1` on both cells. Those exact cells remain after a
clean save plus fresh login.

This map is distinct from M178's single gold-disc insert.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+jukebox84x2|cause=packet15-item84-place+packet15-disc2256+packet15-disc2257|wire=packet61-instrument1005-pitch2256+packet61-instrument1005-pitch2257|oracle=official-record-set+fresh-login-block84|column=17,support=4:71:4:1:0+5:71:4:1:0,jukebox=4:72:4:84:1+5:72:4:84:1,disc=2256->empty+2257->empty,play=packet61:1005:2256+packet61:1005:2257,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b139e039c60f517453a6e8e0c3fe4f87b11f5c73faa81a77c7fceb7645428d53`.
