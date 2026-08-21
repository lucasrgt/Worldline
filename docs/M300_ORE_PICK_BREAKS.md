# M300 ore pick breaks

M300 opens the official ore-and-cobble pick harvest family. It reuses
the M208/M225 raised stone fixture so Packet15 places cobble `4:0`,
coal ore `16:0`, and diamond ore `56:0`. Packet14 while holding iron
pickaxe `257` breaks cobble and coal ore to air and drops Packet21
cobblestone `4:1:0` plus coal `263:1:0`. Packet14 while holding diamond
pickaxe `278` breaks diamond ore to air and drops Packet21 diamond
`264:1:0`.

This milestone is distinct from M222 cobble place, M225 coal-ore place,
M228 diamond-ore place, and M269 shears-versus-bare-hand. It reuses the
existing Packet21 tracker. It does not claim pickaxe durability,
bare-hand rejection, fortune, or other ores.
