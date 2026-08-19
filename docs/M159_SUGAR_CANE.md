# M159 sugar cane

M159 opens the official sugar-cane (`BlockReed`) boundary. Item `338` is planted
on dirt adjacent to still water `9:0`. The live session waits official scheduled
random ticks until the stack reaches height `>= 2`, then a fresh login rereads
the same dirt, water and cane cells.

Beta 1.7.3 `canPlaceBlockAt` accepts grass or dirt beside water, or another
reed; sand is not a legal soil. Exact wait length and extra height above 2 are
not hashed, because random-tick clocks are not a frozen delay. The milestone
does not claim sand planting, bone meal, height 3, exact metadata clocks, drop
tables, or water-removal breakage.
