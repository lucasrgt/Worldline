# M139 Water-lava reaction

Status: GO in Worldline v1.127.0.

M139 adds the first bounded official fluid-material reaction. A raised,
stone-confined basin begins with still lava `11:0` beside exact air. A fresh
client places still water `9:0`; vanilla neighbor processing preserves the
water source and converts the adjacent lava source to obsidian `49:0`.

The causal hash covers only those two predeclared cells. Live Packet53 updates
prove both resulting states and a third client proves the same water and
obsidian states after clean disconnect and save. Two fresh official server
JVMs reproduce the exact transition and hash.

M139 does not claim flowing-lava cobblestone production, vertical flow,
arbitrary fluid metadata, buckets, fire, entity damage, cross-chunk reactions,
reaction timing below the bounded window or stability outside the two-cell
causal scope.
