# M181 lava bucket

M181 opens the official lava-bucket pickup boundary. Still lava `11:0` is
placed from inventory item `11` into a raised four-wall stone basin. The
actor stands on the south wall so the lava cell is never occupied. Empty
bucket `325` then uses Packet15 on that cell, including direction-255
raytrace, so the official `ItemBucket` path can hit the source.

The live oracle is air `0:0` in the basin cell and Packet103 hotbar
`327:1:0`. The same empty basin and lava bucket persist after save plus
fresh login.

This milestone does not claim flowing-lava pickup, obsidian reaction, or
fire spread.
