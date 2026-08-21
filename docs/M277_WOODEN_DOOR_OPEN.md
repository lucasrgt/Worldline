# M277 wooden door open

M277 opens the official wooden `BlockDoor` hand-toggle-open boundary. Wooden
door item 324 is placed with Packet15 onto a raised stone shelf as two cells:
lower `64:0` and upper `64:8`, the same closed halves as M162. Empty-hand
Packet15 on the lower half opens the door to lower `64:4` / upper `64:12`.
Those open halves remain after a clean save plus fresh login.

Frozen semantic SHA-256:
`1f0b2fd8a64b2092de4a093f2d5cf0c8110b4363e2ee0199faf1ca2ae7ff2eb0`.

This milestone does not claim iron doors (M241 does not toggle by empty
hand), redstone power, M162's close-and-persist-closed cycle, stacking,
hinge facing variants beyond this official placement, or the Nether.
