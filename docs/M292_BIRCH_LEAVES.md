# M292 birch leaves

M292 opens the official birch-leaves placement boundary. Packet15 of oak
log item `17` on the east face of the M175 raised stone, then Packet15 of
leaves item `18` with damage `2` on the top face, places leaf `18:10`
beside log `17:0`. That exact birch leaf cell remains after a clean save
plus fresh login.

Official leaves decay without nearby wood, so the hashed path keeps oak
log `17` adjacent and freezes the leaf identity that actually persists
(`18:10`, birch plus the decay-check bit). This milestone is distinct
from M209 oak `18:8` and spruce `18:9`. It does not claim decay-without-log
as the hashed success path, shear drops, or other leaf species.
