# M117 behavior map

The official fixture first reproduces M116: dust 331 creates wire `55:0`, and
one Packet15 lever activation establishes lever/wire `69:9` / `55:15`.
That exact powered pair is the treatment precondition, not inferred state.

A second empty-hand Packet15 toggles the lever. Packet53 publishes
`69:9 -> 69:1` and `55:15 -> 55:0`. After clean disconnect/save, a fresh
Packet51 must expose both depowered states. The ordered full-chunk delta admits
exactly those two cells.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+lever69+dust331-wire55|settle=200+10ticks|precondition=lever69:9+wire55:15|cause=packet15-lever-activate|effect=packet53-wire-depower|observation=fresh-login-packet51|column=10,lever=5:64:4:9->1,wire=4:65:4:15->0,states=2:b033ed6f394141aa6a6eb797e19e7e82d6b8e81a655f198e38a8dfe16779ba6f|disconnect=clean
```

SHA-256: `87c06977c34465cb580ba9a857102c62e6953ede7cfe339c2730fc9673a699fe`.

Packet15 is request evidence only. Packet53 and the reload Packet51 are the
server-authoritative recovery oracles.
