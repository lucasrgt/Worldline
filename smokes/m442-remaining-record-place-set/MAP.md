<!-- worldline-map-schema=1 -->
<!-- boundary=m442-remaining-record-place-set -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=b70badf841ffc29e7c9adb0c7d29b5c2b687a43a5bcdb0e85e065170d1f7551a -->

# M442 behavior map

Official jukebox item `84` is placed twice on a raised stone support.
Gold disc `2256` (13) is used on the first cell and green disc `2257`
(cat) on the second. The dedicated server emits Packet61 effect `1005`
data `2256` then Packet61 effect `1005` data `2257`, empties both
selected slots, and writes metadata `84:1` on both cells. Those exact
cells remain after a clean save plus fresh login. Packet21 eject is
absent.

This map is distinct from M334's play-only record SET and from M398's
Packet21 eject SET. It does not clone M178's one-cell gold-disc insert.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+remaining-jukebox84-place-set|cause=packet15-item84-place+packet15-disc2256+packet15-disc2257|wire=packet61-instrument1005-pitch2256+packet61-instrument1005-pitch2257|oracle=official-remaining-record-place-set+fresh-login-jukebox84|column=17,support=4:71:4:1:0+5:71:4:1:0,place=84x2,jukebox=4:72:4:84:1+5:72:4:84:1,insert=2256->empty+2257->empty,play=packet61:1005:2256+packet61:1005:2257,eject=none,persisted=true,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`b70badf841ffc29e7c9adb0c7d29b5c2b687a43a5bcdb0e85e065170d1f7551a`.
