# M254 water place

M254 opens the official water-bucket place boundary, the inverse of M168.
A raised four-wall stone basin is built with empty air above the support.
Water bucket `326` then uses Packet15 on that cell, including direction-255
raytrace, so the official `ItemBucket` path can write still water `9:0`.

The live oracle is still water `9:0` in the basin cell and Packet103 hotbar
`325:1:0`. The same still water and empty bucket persist after save plus
fresh login.

This milestone does not claim flowing-water placement, ice, milk, or picking
the water back up.
