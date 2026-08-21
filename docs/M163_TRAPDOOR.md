# M163 trapdoor

M163 opens the official wooden-trapdoor boundary. Item 96 is placed against a
raised stone east face as block `96:3`. Empty-hand Packet15 toggles that cell
open to `96:7` and closed again to `96:3`. The closed metadata persists across
a clean save plus fresh login.

Beta 1.7.3 trapdoors attach only to the four side faces and rest on the bottom
of the cell when closed. Open state is metadata bit `4`. This milestone does
not claim redstone, waterlogging (absent in this version), or iron trapdoors
(absent in this version).
