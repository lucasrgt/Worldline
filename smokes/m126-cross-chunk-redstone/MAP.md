# M126 behavior map

A stone column ends at global x=15. Redstone dust `55:0` sits above the final
source-chunk block; a side lever `69:1` occupies global x=16 in the east chunk.
After 200 stabilization heartbeats, Packet15 activates the neighboring lever.

Packet53 and fresh complete Packet51 snapshots prove lever `1→9` and wire
`0→15`. Each chunk has exactly one state delta.

Frozen semantic SHA-256:
`1464edc1c01b62563d3608f6b60b9ba6ee30470dbf16e2111ac2c2cd59e880e5`.
