<!-- worldline-map-schema=1 -->
<!-- boundary=m146-obsidian-piston-rejection -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=5deacfe1aa98b05c6667cd13215354e232659bd057f50e1340640017dface768 -->

# M146 behavior map

The raised west-facing normal piston `33:4` has obsidian `49:0` immediately in
front and air in the destination cell. One lever activation powers the lever
from metadata `1` to `9`, while the piston remains `33:4`, obsidian remains
`49:0`, and the destination remains `0:0`.

The exact raised-volume delta is therefore the single lever cell. A clean save
and fresh Packet51 observation confirm the same final states.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+piston33-west+obsidian49+lever69|settle=200+10ticks|cause=packet15-lever-activate|effect=official-obsidian-push-rejection|observation=fresh-login-packet51|column=10,lever=5:64:4:1->9,piston=4:65:4:4->4,payload=49:0->49:0,destination=0:0->0:0,raised-states=1:3506bb3866a86782ddacfae92e7468ec72d1874777e768650eb4be95b8810c85|disconnect=clean
```

Frozen semantic SHA-256:
`5deacfe1aa98b05c6667cd13215354e232659bd057f50e1340640017dface768`.
