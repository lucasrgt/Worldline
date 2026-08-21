# M125 cross-chunk water

Status: GO in Worldline v1.113.0.

M125 builds a bounded stone trench directly across the east edge of chunk
`(0,0)`. Exact source water `9:0` sits at global x=15 while dirt `3:0` blocks
the only legal destination at x=16 in chunk `(1,0)`.

Packet14 opens the gate, Packet53 confirms air, and official scheduled updates
produce target `9:1`. A fresh reader proves the source chunk has zero state
changes and the neighbor has exactly the target change.

M125 proves one eastward cross-chunk fluid transition. It does not claim
branching flow, distance decay, source creation, unloaded chunks, other
directions, lava, mixing, tick-exact timing, or a Worldline fluid engine.
