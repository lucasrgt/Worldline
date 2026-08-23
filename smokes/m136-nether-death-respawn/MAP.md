<!-- worldline-map-schema=1 -->
<!-- boundary=m136-nether-death-respawn -->
<!-- nonclaims=bounded-to-qualified-evidence -->
<!-- frozen-trace=48c243301cfa00388490bde784ac80eb7597256aa539b83f1777b841d77148a1 -->

# M136 behavior map

One empty player is seeded in dimension `-1` at Y `-80`. Before treatment the
client decodes chunk `(0,0)`, requires netherrack and zero positive skylight
samples, then lets official void damage drive health nonpositive.

The exact request is Packet9 plus signed byte `-1`. A newer inbound Packet9
must publish dimension `0`, followed by Packet8 health `20`. Because the
dimension changes, the remote cache is reset; the post-correction view must
contain only chunks with positive Overworld skylight. Save persists `0:20`.

Frozen semantic SHA-256:
`48c243301cfa00388490bde784ac80eb7597256aa539b83f1777b841d77148a1`.
