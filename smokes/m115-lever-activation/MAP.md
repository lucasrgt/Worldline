# M115 behavior map

The player NBT contains sixteen stone blocks and one lever in distinct hotbar
slots. Packet16 selects each item and Packet15 constructs a column through the
generated water to air at Y64. Packet53 confirms each server-authored block.
After 200 heartbeat ticks, the immutable snapshot contains lever `69:1`.

The actor selects empty hotbar slot two and calls the new activation boundary.
Its Packet15 contains the lever coordinate, UP face and null item sentinel.
The existing incremental cache may change only through the inbound Packet53.
After clean disconnect/save, a second client's Packet51 must contain `69:9`.

Frozen trace:

```text
v1|server=official-b1.7.3|seed=17320110707|fixture=stone-column+lever69|settle=200ticks|cause=empty-hand-packet15-activate|confirmation=packet53|observation=fresh-login-packet51|column=10,lever=5:64:4,off=69:1,on=69:9,states=1:3506bb3866a86782ddacfae92e7468ec72d1874777e768650eb4be95b8810c85|disconnect=clean
```

SHA-256: `497b5d743a5693c925d69d71c02528cf2d16a63ad5c477980b916a0d2b45ae34`.

This is one exact empty-hand lever transition. Packet15 itself is not an ACK;
the Packet53 state change and fresh Packet51 are the official oracles.
