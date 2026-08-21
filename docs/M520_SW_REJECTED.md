# M520-SW rejected: furnace fuel remainder

M520-SW is not a valid Beta 1.7.3 milestone. Direct bytecode inspection of the
official server JAR maps `TileEntityFurnace` to class `ln`. Its update path
decrements the stack in fuel slot 1 and writes `null` when the count reaches
zero. It does not invoke the item's container-item method, even though the lava
bucket item itself declares an empty bucket as its container item for other
craft/use paths.

M338 already freezes the relevant official furnace behavior: lava bucket 327
provides burn time 20000, smelts cobblestone to stone, and leaves fuel slot 1
empty. Coal and planks are frozen beside it. Adding an empty-bucket expectation
would import later-version behavior and contradict the official oracle.

No implementation, tag, or release should be created for M520-SW. The archived
queue and this rejection record preserve why the identifier was retired.
