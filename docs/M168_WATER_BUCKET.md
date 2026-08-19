# M168 water bucket

M168 opens the official empty-bucket pickup boundary. Still water `9:0` is
placed from inventory item `9` into a raised four-wall stone basin. Empty
bucket `325` then uses Packet15 on that cell, including direction-255
raytrace, so the official `ItemBucket` path can hit the source.

The live oracle is air `0:0` in the basin cell and Packet103 hotbar
`326:1:0`. The same empty basin and water bucket persist after save plus
fresh login.

This milestone does not claim flowing-water pickup, lava buckets, milk, or
placing the water back.
