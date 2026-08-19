# M116 behavior map

The official player NBT contains stone 1, lever 69 and redstone dust 331. The
adapter constructs Packet15 for dust from the observed inventory stack; the
server's item behavior creates wire `55:0` above the stabilized column.

After the actor selects an empty hand, M115's activation boundary toggles the
side lever. Packet53 publishes both `69:1 -> 69:9` and `55:0 -> 55:15`.
After clean disconnect/save, a fresh Packet51 must expose both powered states.
The full-chunk ordered state delta admits exactly those two cells.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+lever69+dust331-wire55|settle=200+10ticks|cause=packet15-lever-activate|effect=packet53-wire-power|observation=fresh-login-packet51|column=10,lever=5:64:4:1->9,wire=4:65:4:0->15,states=2:5f8ada70879cd4ae2c504a2bafb3664d468caa4a7f2c7e4caaf119347c7d65b9|disconnect=clean
```

SHA-256: `973fb75a9541e4f8015d8133d7c99779e6c1ab8b6ef095120609e6a6fcab5587`.

Packet15 is request evidence only. Packet53 and the reload Packet51 are the
server-authoritative propagation oracles.
