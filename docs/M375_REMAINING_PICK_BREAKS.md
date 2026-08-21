# M375 remaining pick breaks

M375 opens the official remaining hard-block pick harvest family. It
reuses the raised stone fixture so Packet15 places mossy cobble `48:0`,
gold ore `14:0`, and obsidian `49:0`. Packet14 while holding gold
pickaxe `285` breaks mossy cobble to air and drops Packet21 mossy cobble
`48:1:0`. Packet14 while holding diamond pickaxe `278` breaks gold ore
and obsidian to air and drops Packet21 gold ore `14:1:0` plus obsidian
`49:1:0`. Diamond ore `56` stays in M300.

This milestone is distinct from M300 iron-pick cobble/coal/diamond-ore,
M216 obsidian place, M217 mossy-cobble place, and M227 gold-ore place.
It reuses the existing Packet21 tracker. It does not claim pickaxe
durability, bare-hand rejection, fortune, or other remaining ores.

The frozen semantic SHA-256 is
`22503c04e191d5edd6c2374799f5062269ff1e38d71c15709e468a2d2e787869`.
