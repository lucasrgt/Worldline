# M125 behavior map

A stone trench spans chunks `(0,0)` and `(1,0)`. Source water `9:0` occupies
global `(15,65,3)` and dirt `3:0` gates `(16,65,3)`. After 200 stabilization
heartbeats, Packet14 removes the gate and Packet53 confirms transient air.

Forty heartbeats produce exact target water `9:1`. Fresh complete snapshots
show no changed states in the source chunk and exactly one changed state in the
neighboring chunk.

Frozen semantic SHA-256:
`c876ddf9f8686e16db848fb38977ff02ea8eb97dea05e21b0837be68f83a6217`.
