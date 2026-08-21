# M398 behavior map

Official jukebox item `84` is placed twice on a raised stone support. Gold
disc `2256` (13) is used on the first cell and green disc `2257` (cat) on
the second. Packet61 effect `1005` plays each disc id, then Packet14 with
gold axe `286` breaks both `84:1` cells. `BlockJukeBox.onBlockRemoval`
calls `func_28035_b_`, which spawns Packet21 item entities `2256` and
`2257`. Both cells are air after a clean save plus fresh login.

This map is distinct from M334's Packet61 play-only insert and from
M178's single gold-disc insert. The frozen signal names both disc ids
and Packet21 eject.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=raised-stone+jukebox84x2|cause=packet15-item84-place+packet15-disc2256+packet15-disc2257+packet14-goldaxe286|wire=packet61-instrument1005-pitch2256+packet61-instrument1005-pitch2257+packet21-2256+packet21-2257|oracle=official-jukebox-eject-set+fresh-login-air|column=17,support=4:71:4:1:0+5:71:4:1:0,jukebox=4:72:4:84:1->0:0+5:72:4:84:1->0:0,disc=2256->empty+2257->empty,play=packet61:1005:2256+packet61:1005:2257,eject=packet21:2256+packet21:2257,persisted=air,clients=2,disconnect=clean
```

Frozen semantic SHA-256:
`21d9a2123e3a3041573a22722d268dec75ee1d0d27d84fe0ae6f22e187f2bd8f`.
