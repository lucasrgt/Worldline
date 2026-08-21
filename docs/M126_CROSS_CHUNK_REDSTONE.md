# M126 cross-chunk redstone

Status: GO in Worldline v1.114.0.

M126 places the controlled lever and its powered wire on opposite sides of a
chunk seam. Lever `(16,64,3)` belongs to chunk `(1,0)` and wire `(15,65,3)` to
chunk `(0,0)`.

One Packet15 activation changes lever metadata 1 to 9 and publishes wire power
0 to 15. Fresh snapshots reproduce exactly one state delta in each chunk.

M126 proves one lever-to-wire signal across an east seam. It does not claim
arbitrary redstone topology, wire decay, repeaters, torches, consumers,
unloaded chunks, timing order, or a Worldline redstone evaluator.
